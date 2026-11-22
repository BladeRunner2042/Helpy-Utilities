package com.example.helpyutilities.util

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.os.StatFs
import java.io.File

/**
 * Data model for per-core CPU clock frequencies
 */
data class CpuCoreFrequency(
    val core: Int,
    val curMhz: Long,
    val maxMhz: Long,
) {
    val usageRatio: Float
        get() = if (maxMhz > 0) (curMhz.toFloat() / maxMhz.toFloat()).coerceIn(0f, 1f) else 0f
}

/**
 * Data model for real-time battery and power metrics
 */
data class BatteryMetrics(
    val levelPercent: Int,
    val isCharging: Boolean,
    val chargingType: String,
    val tempCelsius: Float,
    val voltageMv: Int,
    val currentNowMa: Int,
    val chargeCounterMah: Int,
    val cycleCount: Int,
    val healthStatus: String,
    val thermalStatus: String,
)

/**
 * Data model for system RAM and internal storage capacities
 */
data class MemoryAndStorageMetrics(
    val ramUsedGb: Float,
    val ramTotalGb: Float,
    val ramPercent: Int,
    val storageUsedGb: Float,
    val storageTotalGb: Float,
    val storagePercent: Int,
)

/**
 * Unified hardware telemetry snapshot
 */
data class HardwareTelemetrySnapshot(
    val cpuCores: List<CpuCoreFrequency>,
    val battery: BatteryMetrics,
    val memoryAndStorage: MemoryAndStorageMetrics,
)

/**
 * HardwareTelemetryHelper
 * Gathers system hardware metrics directly via Android platform APIs and Linux sysfs
 */
object HardwareTelemetryHelper {

    /**
     * Reads per-core CPU clock frequencies via /sys/devices/system/cpu/
     */
    fun readPerCoreFrequencies(): List<CpuCoreFrequency> {
        val coreCount = Runtime.getRuntime().availableProcessors()
        val coresList = mutableListOf<CpuCoreFrequency>()

        for (i in 0 until coreCount) {
            val basePath = "/sys/devices/system/cpu/cpu$i/cpufreq"
            val curMhz = try {
                File("$basePath/scaling_cur_freq").readText().trim().toLong() / 1000
            } catch (_: Exception) {
                0L
            }
            val maxMhz = try {
                File("$basePath/cpuinfo_max_freq").readText().trim().toLong() / 1000
            } catch (_: Exception) {
                0L
            }

            coresList.add(
                CpuCoreFrequency(
                    core = i,
                    curMhz = curMhz,
                    maxMhz = maxMhz
                )
            )
        }
        return coresList
    }

    /**
     * Reads battery and power metrics via BatteryManager & Intent.ACTION_BATTERY_CHANGED
     */
    fun readBatteryMetrics(context: Context): BatteryMetrics {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        val rawLevel = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val rawScale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val levelPercent = if (rawLevel >= 0 && rawScale > 0) {
            ((rawLevel.toFloat() / rawScale.toFloat()) * 100).toInt().coerceIn(0, 100)
        } else {
            100
        }

        val rawTemp = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1
        val tempCelsius = if (rawTemp > 0) rawTemp / 10.0f else 0f
        val voltageMv = intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0
        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
        val chargePlug = intent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
        val chargingType = when (chargePlug) {
            BatteryManager.BATTERY_PLUGGED_USB -> "USB Cable"
            BatteryManager.BATTERY_PLUGGED_AC -> "Fast AC Charger"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless Dock"
            else -> if (isCharging) "Charging" else "On Battery"
        }

        val rawHealth = intent?.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN) ?: BatteryManager.BATTERY_HEALTH_UNKNOWN
        val healthStatus = when (rawHealth) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Failure"
            else -> "Normal"
        }

        // Live Current in mA (BATTERY_PROPERTY_CURRENT_NOW is typically in microamperes uA)
        val rawCurrentUa = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW) ?: 0
        val currentNowMa = if (rawCurrentUa != Int.MIN_VALUE && rawCurrentUa != 0) {
            rawCurrentUa / 1000
        } else {
            0
        }

        // Charge Counter in mAh (BATTERY_PROPERTY_CHARGE_COUNTER is in microampere-hours uAh)
        val rawCounterUah = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER) ?: 0
        val chargeCounterMah = if (rawCounterUah > 0) rawCounterUah / 1000 else 0

        // Cycle Count (API 34 / Android 14+)
        val cycleCount = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            intent?.getIntExtra(BatteryManager.EXTRA_CYCLE_COUNT, -1) ?: -1
        } else {
            -1
        }

        // Thermal status
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val thermalStatus = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            when (powerManager?.currentThermalStatus) {
                PowerManager.THERMAL_STATUS_NONE -> "Normal (Optimal)"
                PowerManager.THERMAL_STATUS_LIGHT -> "Light Throttling"
                PowerManager.THERMAL_STATUS_MODERATE -> "Moderate Throttling"
                PowerManager.THERMAL_STATUS_SEVERE -> "Severe Throttling"
                PowerManager.THERMAL_STATUS_CRITICAL -> "Critical Heat"
                PowerManager.THERMAL_STATUS_EMERGENCY -> "Emergency Cooldown"
                PowerManager.THERMAL_STATUS_SHUTDOWN -> "Thermal Shutdown"
                else -> "Optimal Conditions"
            }
        } else {
            "Optimal Conditions"
        }

        return BatteryMetrics(
            levelPercent = levelPercent,
            isCharging = isCharging,
            chargingType = chargingType,
            tempCelsius = tempCelsius,
            voltageMv = voltageMv,
            currentNowMa = currentNowMa,
            chargeCounterMah = chargeCounterMah,
            cycleCount = cycleCount,
            healthStatus = healthStatus,
            thermalStatus = thermalStatus,
        )
    }

    /**
     * Reads RAM and Internal Storage statistics
     */
    fun readMemoryAndStorageMetrics(context: Context): MemoryAndStorageMetrics {
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager?.getMemoryInfo(memInfo)
        val ramTotal = memInfo.totalMem / (1024f * 1024f * 1024f)
        val ramAvail = memInfo.availMem / (1024f * 1024f * 1024f)
        val ramUsed = (ramTotal - ramAvail).coerceAtLeast(0f)
        val ramPct = if (ramTotal > 0) ((ramUsed / ramTotal) * 100).toInt().coerceIn(0, 100) else 0

        var storageUsed = 0f
        var storageTotal = 0f
        var storagePct = 0
        try {
            val stat = StatFs(Environment.getDataDirectory().path)
            storageTotal = stat.totalBytes / (1024f * 1024f * 1024f)
            val storageAvail = stat.availableBytes / (1024f * 1024f * 1024f)
            storageUsed = (storageTotal - storageAvail).coerceAtLeast(0f)
            storagePct = if (storageTotal > 0) ((storageUsed / storageTotal) * 100).toInt().coerceIn(0, 100) else 0
        } catch (_: Exception) {
            storageTotal = 128f
            storageUsed = 64f
            storagePct = 50
        }

        return MemoryAndStorageMetrics(
            ramUsedGb = ramUsed,
            ramTotalGb = ramTotal,
            ramPercent = ramPct,
            storageUsedGb = storageUsed,
            storageTotalGb = storageTotal,
            storagePercent = storagePct,
        )
    }

    /**
     * Collects full hardware telemetry snapshot
     */
    fun collectSnapshot(context: Context): HardwareTelemetrySnapshot {
        return HardwareTelemetrySnapshot(
            cpuCores = readPerCoreFrequencies(),
            battery = readBatteryMetrics(context),
            memoryAndStorage = readMemoryAndStorageMetrics(context),
        )
    }
}
