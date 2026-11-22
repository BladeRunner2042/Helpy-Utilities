package com.example.helpyutilities.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class UpdateResult(
    val hasUpdate: Boolean,
    val latestVersion: String,
    val currentVersion: String = "1.0",
    val releaseTitle: String = "",
    val releaseNotes: String = "",
    val downloadUrl: String = "",
    val githubUrl: String = "https://github.com/megan/HelpyUtilities"
)

object UpdateChecker {
    private const val CHANNEL_ID = "helpy_updates_channel"
    private const val CURRENT_VERSION = "1.0.0"
    // GitHub API endpoint
    private const val GITHUB_REPO_API = "https://github.com"
    private const val GITHUB_REPO_PAGE = "https://github.com"

    suspend fun check(currentVersion: String = CURRENT_VERSION): UpdateResult {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL(GITHUB_REPO_API)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.setRequestProperty("User-Agent", "HelpyUtilities-App")
                conn.connectTimeout = 5000
                conn.readTimeout = 5000

                if (conn.responseCode == 200) {
                    val body = conn.inputStream.bufferedReader().readText()
                    val json = JSONObject(body)
                    val tagName = json.optString("tag_name", "").removePrefix("v")
                    val name = json.optString("name", "New Version")
                    val bodyNotes = json.optString("body", "Bug fixes and performance improvements.")
                    val htmlUrl = json.optString("html_url", GITHUB_REPO_PAGE)

                    var apkDownloadUrl = htmlUrl
                    if (json.has("assets")) {
                        val assets = json.getJSONArray("assets")
                        for (i in 0 until assets.length()) {
                            val asset = assets.getJSONObject(i)
                            val download = asset.optString("browser_download_url", "")
                            if (download.endsWith(".apk")) {
                                apkDownloadUrl = download
                                break
                            }
                        }
                    }

                    val isNewer = isVersionNewer(tagName, currentVersion)
                    UpdateResult(
                        hasUpdate = isNewer,
                        latestVersion = if (tagName.isNotBlank()) "v$tagName" else "v$currentVersion",
                        currentVersion = "v$currentVersion",
                        releaseTitle = name,
                        releaseNotes = bodyNotes,
                        downloadUrl = apkDownloadUrl,
                        githubUrl = htmlUrl
                    )
                } else {
                    // If repo is private or doesn't have releases yet, return up to date gracefully
                    UpdateResult(
                        hasUpdate = false,
                        latestVersion = "v$currentVersion",
                        currentVersion = "v$currentVersion",
                        releaseTitle = "Up to date",
                        releaseNotes = "You are currently running the latest version of Helpy Utilities.",
                        githubUrl = GITHUB_REPO_PAGE
                    )
                }
            } catch (_: Exception) {
                UpdateResult(
                    hasUpdate = false,
                    latestVersion = "v$currentVersion",
                    currentVersion = "v$currentVersion",
                    releaseTitle = "Check failed",
                    releaseNotes = "Could not connect to GitHub. Please check your internet connection.",
                    githubUrl = GITHUB_REPO_PAGE
                )
            }
        }
    }

    private fun isVersionNewer(remote: String, current: String): Boolean {
        if (remote.isBlank()) return false
        val remParts = remote.split(".").mapNotNull { it.toIntOrNull() }
        val curParts = current.split(".").mapNotNull { it.toIntOrNull() }
        for (i in 0 until maxOf(remParts.size, curParts.size)) {
            val r = remParts.getOrElse(i) { 0 }
            val c = curParts.getOrElse(i) { 0 }
            if (r > c) return true
            if (r < c) return false
        }
        return false
    }

    fun showUpdateNotification(context: Context, update: UpdateResult) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Software Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for new Helpy Utilities releases on GitHub"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val targetUrl = update.downloadUrl.ifBlank { update.githubUrl }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl))
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("New Version Available: ${update.latestVersion}")
            .setContentText("A new release of Helpy Utilities is available on GitHub.")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Version ${update.latestVersion} is out!\n${update.releaseNotes}"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1001, notification)
    }
}
