package es.antonborri.home_widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri

object HomeWidgetLaunchIntent {
    fun getActivity(context: Context, activityClass: Class<*>, uri: Uri): PendingIntent {
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setClass(context, activityClass)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(
            context,
            uri.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}

abstract class HomeWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        val flutterPrefs = context.getSharedPreferences("FlutterSharedPreferences", Context.MODE_PRIVATE)
        val appPrefs = context.getSharedPreferences("helpy_app_prefs", Context.MODE_PRIVATE)
        val data = if (flutterPrefs.all.isNotEmpty()) flutterPrefs else appPrefs
        onUpdate(context, appWidgetManager, appWidgetIds, data)
    }

    abstract fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
        widgetData: SharedPreferences
    )
}
