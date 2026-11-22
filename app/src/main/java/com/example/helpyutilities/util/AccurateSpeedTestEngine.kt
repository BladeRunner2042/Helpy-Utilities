package com.example.helpyutilities.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max

data class AccurateSpeedResult(
    val downloadMbps: Double,
    val uploadMbps: Double,
    val pingMs: Double,
    val jitterMs: Double
)

object AccurateSpeedTestEngine {
    private const val PARALLEL_DOWNLOAD_STREAMS = 6
    private const val PARALLEL_UPLOAD_STREAMS = 4
    private const val WARM_UP_MS = 1500L
    private const val MEASURE_DURATION_MS = 7000L

    /**
     * 1. ACCURATE PING & JITTER (Discards DNS/TLS cold-start probe)
     */
    suspend fun measureLatency(
        isCancelled: () -> Boolean,
        isDataSaver: Boolean = false
    ): Pair<Double, Double> = withContext(Dispatchers.IO) {
        val probeCount = if (isDataSaver) 5 else 8
        val rtts = mutableListOf<Double>()

        for (i in 0 until probeCount) {
            if (isCancelled()) break
            val nonce = "${System.currentTimeMillis()}_$i"
            try {
                val url = URL("https://cloudflare.com/__ping?$nonce")
                val start = System.nanoTime()
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "HEAD"
                    connectTimeout = 3000
                    readTimeout = 3000
                    useCaches = false
                    instanceFollowRedirects = true
                }
                conn.responseCode
                conn.disconnect()
                val elapsedMs = (System.nanoTime() - start) / 1_000_000.0

                // Skip probe 0 because it includes DNS resolution & TLS handshake
                if (i > 0) {
                    rtts.add(elapsedMs)
                }
            } catch (_: Exception) {
                // Ignore probe retry
            }
        }

        if (rtts.isEmpty()) return@withContext Pair(0.0, 0.0)

        // Use minimum/lower-quartile RTT for true unloaded ping (Ookla standard)
        val sorted = rtts.sorted()
        val ping = sorted.first()

        var jitterSum = 0.0
        for (i in 1 until rtts.size) {
            jitterSum += abs(rtts[i] - rtts[i - 1])
        }
        val jitter = if (rtts.size > 1) jitterSum / (rtts.size - 1) else 0.0

        Pair(ping, jitter)
    }

    /**
     * 2. MULTI-STREAM DOWNLOAD TEST (Parallel Streams + Warm-Up Discard + Trimmed Mean)
     */
    suspend fun measureDownload(
        onProgress: (Double) -> Unit,
        isCancelled: () -> Boolean,
        isDataSaver: Boolean = false
    ): Double = withContext(Dispatchers.IO) {
        val streamCount = if (isDataSaver) 2 else PARALLEL_DOWNLOAD_STREAMS
        val warmUpMs = if (isDataSaver) 800L else WARM_UP_MS
        val measureDurationMs = if (isDataSaver) 3200L else MEASURE_DURATION_MS
        val totalTargetMs = warmUpMs + measureDurationMs

        val steadyStateBytes = AtomicLong(0L)
        val warmUpComplete = AtomicBoolean(false)
        val totalStart = System.currentTimeMillis()
        var steadyStart = 0L

        val intervalSamples = mutableListOf<Double>()
        var lastSliceBytes = 0L
        var lastSliceMs = 0L

        coroutineScope {
            // Sampler job: samples throughput every 250ms for smooth gauge updates & trimmed mean
            val samplerJob = launch {
                while (isActive && !isCancelled() && (System.currentTimeMillis() - totalStart) < totalTargetMs) {
                    delay(250)
                    val totalElapsed = System.currentTimeMillis() - totalStart

                    if (!warmUpComplete.get() && totalElapsed >= warmUpMs) {
                        warmUpComplete.set(true)
                        steadyStateBytes.set(0L)
                        lastSliceBytes = 0L
                        lastSliceMs = 0L
                        steadyStart = System.currentTimeMillis()
                        continue
                    }

                    if (warmUpComplete.get() && steadyStart > 0) {
                        val nowMs = System.currentTimeMillis() - steadyStart
                        val deltaMs = nowMs - lastSliceMs
                        val currentSteadyBytes = steadyStateBytes.get()
                        val deltaBytes = currentSteadyBytes - lastSliceBytes

                        if (deltaMs > 0) {
                            val instantMbps = (deltaBytes * 8.0) / (deltaMs * 1000.0)
                            synchronized(intervalSamples) {
                                intervalSamples.add(instantMbps)
                            }
                            withContext(Dispatchers.Main) {
                                onProgress(instantMbps)
                            }
                            lastSliceMs = nowMs
                            lastSliceBytes = currentSteadyBytes
                        }
                    }
                }
            }

            // Parallel download streams
            val streamJobs = (0 until streamCount).map { streamIndex ->
                launch {
                    val buffer = ByteArray(32768)
                    while (isActive && !isCancelled() && (System.currentTimeMillis() - totalStart) < totalTargetMs) {
                        var conn: HttpURLConnection? = null
                        var input: InputStream? = null
                        try {
                            val nonce = "${System.currentTimeMillis()}_${streamIndex}_${System.nanoTime()}"
                            val url = URL("https://cloudflare.com/__speed?$nonce")
                            conn = (url.openConnection() as HttpURLConnection).apply {
                                requestMethod = "GET"
                                connectTimeout = 4000
                                readTimeout = 5000
                                useCaches = false
                            }
                            input = conn.inputStream
                            while (isActive && !isCancelled() && (System.currentTimeMillis() - totalStart) < totalTargetMs) {
                                val read = input.read(buffer)
                                if (read == -1) break
                                if (warmUpComplete.get()) {
                                    steadyStateBytes.addAndGet(read.toLong())
                                }
                            }
                        } catch (_: Exception) {
                            delay(100)
                        } finally {
                            try { input?.close() } catch (_: Exception) {}
                            try { conn?.disconnect() } catch (_: Exception) {}
                        }
                    }
                }
            }

            streamJobs.joinAll()
            samplerJob.cancel()
        }

        calculateTrimmedMean(intervalSamples)
    }

    /**
     * 3. MULTI-STREAM UPLOAD TEST (Parallel Streams + Warm-Up Discard + Trimmed Mean)
     */
    suspend fun measureUpload(
        onProgress: (Double) -> Unit,
        isCancelled: () -> Boolean,
        isDataSaver: Boolean = false
    ): Double = withContext(Dispatchers.IO) {
        val workerCount = if (isDataSaver) 2 else PARALLEL_UPLOAD_STREAMS
        val warmUpMs = if (isDataSaver) 800L else WARM_UP_MS
        val measureDurationMs = if (isDataSaver) 3000L else MEASURE_DURATION_MS
        val totalTargetMs = warmUpMs + measureDurationMs
        val payloadSize = (if (isDataSaver) 1 else 2) * 1024 * 1024
        val payload = ByteArray(payloadSize) { 0x55.toByte() }

        val steadyStateBytes = AtomicLong(0L)
        val warmUpComplete = AtomicBoolean(false)
        val totalStart = System.currentTimeMillis()
        var steadyStart = 0L
        val intervalSamples = mutableListOf<Double>()

        coroutineScope {
            val uploadJobs = (0 until workerCount).map { workerIndex ->
                launch {
                    while (isActive && !isCancelled() && (System.currentTimeMillis() - totalStart) < totalTargetMs) {
                        var conn: HttpURLConnection? = null
                        var os: OutputStream? = null
                        try {
                            val nonce = "${System.currentTimeMillis()}_${workerIndex}_${System.nanoTime()}"
                            val url = URL("https://cloudflare.com/__speed_up?$nonce")
                            conn = (url.openConnection() as HttpURLConnection).apply {
                                requestMethod = "POST"
                                doOutput = true
                                connectTimeout = 4000
                                readTimeout = 4000
                                useCaches = false
                                setFixedLengthStreamingMode(payload.size)
                            }
                            os = conn.outputStream
                            os.write(payload)
                            os.flush()

                            val totalElapsed = System.currentTimeMillis() - totalStart
                            if (!warmUpComplete.get() && totalElapsed >= warmUpMs) {
                                warmUpComplete.set(true)
                                steadyStart = System.currentTimeMillis()
                                steadyStateBytes.set(0L)
                            } else if (warmUpComplete.get() && steadyStart > 0) {
                                val currentSteady = steadyStateBytes.addAndGet(payload.size.toLong())
                                val elapsedMs = System.currentTimeMillis() - steadyStart
                                if (elapsedMs > 0) {
                                    val currentMbps = (currentSteady * 8.0) / (elapsedMs * 1000.0)
                                    synchronized(intervalSamples) {
                                        intervalSamples.add(currentMbps)
                                    }
                                    withContext(Dispatchers.Main) {
                                        onProgress(currentMbps)
                                    }
                                }
                            }
                        } catch (_: Exception) {
                            delay(100)
                        } finally {
                            try { os?.close() } catch (_: Exception) {}
                            try { conn?.disconnect() } catch (_: Exception) {}
                        }
                    }
                }
            }

            uploadJobs.joinAll()
        }

        calculateTrimmedMean(intervalSamples)
    }

    /**
     * Discards slowest 20% and fastest 10% of samples for Ookla-grade stability
     */
    fun calculateTrimmedMean(samples: List<Double>): Double {
        if (samples.isEmpty()) return 0.0
        if (samples.size < 5) {
            return samples.average()
        }

        val sorted = samples.sorted()
        val startIdx = (sorted.size * 0.20).toInt()
        val endIdx = max(startIdx + 1, ceil(sorted.size * 0.90).toInt()).coerceAtMost(sorted.size)
        val trimmed = sorted.subList(startIdx, endIdx)

        return if (trimmed.isNotEmpty()) trimmed.average() else sorted.average()
    }
}
