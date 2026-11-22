package com.example.helpyutilities

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.View
import android.widget.RemoteViews
import es.antonborri.home_widget.HomeWidgetProvider

class HelpyQuickLaunchWidgetProvider : HomeWidgetProvider() {

    companion object {
        const val ACTION_LAUNCH_TOOL = "com.example.helpyutilities.widget.ACTION_LAUNCH_TOOL"
        const val ACTION_WIDGET_ENTER_IDLE = "com.example.helpyutilities.widget.ACTION_WIDGET_ENTER_IDLE"
        const val ACTION_RESET_IDLE = "com.example.helpyutilities.widget.ACTION_RESET_QUICK_IDLE"
        const val EXTRA_TOOL_NAME = "extra_tool_name"
        private const val PREFS_IDLE_KEY = "quick_launch_is_idle"
        private const val REQ_CODE_RESET = 8011
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_LAUNCH_TOOL -> {
                val data = intent.data
                if (data != null) {
                    val launchIntent = Intent(Intent.ACTION_VIEW, data).apply {
                        setClass(context, MainActivity::class.java)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }
                    context.startActivity(launchIntent)
                }

                // Active / Wake State: rocket fades out, badge pulses "LAUNCHING..."
                setIdleState(context, isIdle = false)
                scheduleIdleReset(context)
                refreshWidgets(context)
                return
            }
            ACTION_WIDGET_ENTER_IDLE, ACTION_RESET_IDLE -> {
                // Return to idle state: rocket glides smoothly, badge resets to "QUICK TOOLS"
                setIdleState(context, isIdle = true)
                refreshWidgets(context)
                return
            }
        }
        super.onReceive(context, intent)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
        widgetData: SharedPreferences,
    ) {
        val bgHex = widgetData.getString("bg_hex", "#FF181B22") ?: "#FF181B22"
        val primaryHex = widgetData.getString("primary_hex", "#FF3B82F6") ?: "#FF3B82F6"
        val textHex = widgetData.getString("text_hex", "#FFF3F4F6") ?: "#FFF3F4F6"
        val isIdle = isCurrentlyIdle(context, widgetData)

        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_quick_launch).apply {
                // Apply synced Material You colors dynamically
                try {
                    val bgColor = Color.parseColor(bgHex)
                    val textColor = Color.parseColor(textHex)
                    val accentColor = Color.parseColor(primaryHex)

                    setInt(R.id.widget_bg_shape, "setColorFilter", bgColor)
                    setInt(R.id.widget_root, "setBackgroundColor", Color.TRANSPARENT)
                    setTextColor(R.id.widget_title, textColor)
                    setTextColor(R.id.widget_badge, accentColor)

                    setTextColor(R.id.label_torch, textColor)
                    setTextColor(R.id.label_calc, textColor)
                    setTextColor(R.id.label_translate, textColor)
                    setTextColor(R.id.label_speed, textColor)

                    setInt(R.id.icon_torch, "setColorFilter", accentColor)
                    setInt(R.id.icon_calc, "setColorFilter", accentColor)
                    setInt(R.id.icon_translate, "setColorFilter", accentColor)
                    setInt(R.id.icon_speed, "setColorFilter", accentColor)

                    // Full-Bleed Idle Rocket & Star Streaks: Tinted with active primaryHex
                    val alphaRocket = Color.argb(204, Color.red(accentColor), Color.green(accentColor), Color.blue(accentColor))
                    setInt(R.id.widget_idle_rocket_full, "setColorFilter", alphaRocket)
                } catch (_: Exception) {
                }

                // Idle vs Active State Handling (Xbox Cloud Gaming Full-Bleed Animation)
                if (isIdle) {
                    setViewVisibility(R.id.widget_idle_rocket_full, View.VISIBLE)
                    setTextViewText(R.id.widget_badge, "QUICK TOOLS")
                } else {
                    setViewVisibility(R.id.widget_idle_rocket_full, View.GONE)
                    setTextViewText(R.id.widget_badge, "LAUNCHING...")
                }

                // Interactive Deep Links with Ripple Feedback & Active Wake State
                setOnClickPendingIntent(
                    R.id.btn_widget_torch,
                    createLaunchPendingIntent(context, "helpy://tool/torch", "Torch")
                )
                setOnClickPendingIntent(
                    R.id.btn_widget_calc,
                    createLaunchPendingIntent(context, "helpy://tool/calculator", "Calculator")
                )
                setOnClickPendingIntent(
                    R.id.btn_widget_translate,
                    createLaunchPendingIntent(context, "helpy://tool/translate", "Translate")
                )
                setOnClickPendingIntent(
                    R.id.btn_widget_speed,
                    createLaunchPendingIntent(context, "helpy://tool/speedtest", "Speed Test")
                )
            }
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    private fun createLaunchPendingIntent(context: Context, uriString: String, toolName: String): PendingIntent {
        val intent = Intent(context, HelpyQuickLaunchWidgetProvider::class.java).apply {
            action = ACTION_LAUNCH_TOOL
            data = Uri.parse(uriString)
            putExtra(EXTRA_TOOL_NAME, toolName)
        }
        return PendingIntent.getBroadcast(
            context,
            uriString.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun isCurrentlyIdle(context: Context, widgetData: SharedPreferences): Boolean {
        val sp = context.getSharedPreferences("helpy_widget_state", Context.MODE_PRIVATE)
        val localIdle = sp.getBoolean(PREFS_IDLE_KEY, true)
        val bridgeIdle = widgetData.getBoolean("is_idle", true)
        return localIdle && bridgeIdle
    }

    private fun setIdleState(context: Context, isIdle: Boolean) {
        val sp = context.getSharedPreferences("helpy_widget_state", Context.MODE_PRIVATE)
        sp.edit().putBoolean(PREFS_IDLE_KEY, isIdle).apply()
    }

    private fun scheduleIdleReset(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        val resetIntent = Intent(context, HelpyQuickLaunchWidgetProvider::class.java).apply {
            action = ACTION_WIDGET_ENTER_IDLE
        }
        val pendingReset = PendingIntent.getBroadcast(
            context,
            REQ_CODE_RESET,
            resetIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            alarmManager?.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 5000,
                pendingReset
            )
        } catch (_: Exception) {
        }

        // In-process fallback timer
        Handler(Looper.getMainLooper()).postDelayed({
            setIdleState(context, isIdle = true)
            refreshWidgets(context)
        }, 5000)
    }

    private fun refreshWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val ids = appWidgetManager.getAppWidgetIds(ComponentName(context, HelpyQuickLaunchWidgetProvider::class.java))
        if (ids != null && ids.isNotEmpty()) {
            val flutterPrefs = context.getSharedPreferences("FlutterSharedPreferences", Context.MODE_PRIVATE)
            val appPrefs = context.getSharedPreferences("helpy_app_prefs", Context.MODE_PRIVATE)
            val data = if (flutterPrefs.all.isNotEmpty()) flutterPrefs else appPrefs
            onUpdate(context, appWidgetManager, ids, data)
        }
    }
}
