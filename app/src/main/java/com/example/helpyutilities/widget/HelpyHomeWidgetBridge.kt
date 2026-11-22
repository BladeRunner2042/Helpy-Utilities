package com.example.helpyutilities.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.example.helpyutilities.HelpyLiveRateWidgetProvider
import com.example.helpyutilities.HelpyQuickLaunchWidgetProvider

object HelpyHomeWidgetBridge {

    fun syncWidgetState(
        context: Context,
        bgHex: String? = null,
        primaryHex: String? = null,
        textHex: String? = null,
        currencyRate: String? = null,
        currencyUpdated: String? = null,
        isIdle: Boolean? = null,
    ) {
        val appPrefs = context.getSharedPreferences("helpy_app_prefs", Context.MODE_PRIVATE)
        val flutterPrefs = context.getSharedPreferences("FlutterSharedPreferences", Context.MODE_PRIVATE)

        val editApp = appPrefs.edit()
        val editFlutter = flutterPrefs.edit()

        bgHex?.let {
            editApp.putString("bg_hex", it)
            editFlutter.putString("bg_hex", it)
        }
        primaryHex?.let {
            editApp.putString("primary_hex", it)
            editFlutter.putString("primary_hex", it)
        }
        textHex?.let {
            editApp.putString("text_hex", it)
            editFlutter.putString("text_hex", it)
        }
        currencyRate?.let {
            editApp.putString("currency_rate", it)
            editFlutter.putString("currency_rate", it)
        }
        currencyUpdated?.let {
            editApp.putString("currency_updated", it)
            editFlutter.putString("currency_updated", it)
        }
        isIdle?.let {
            editApp.putBoolean("is_idle", it)
            editFlutter.putBoolean("is_idle", it)
        }

        editApp.apply()
        editFlutter.apply()

        // Trigger updates across both native widget providers
        val mgr = AppWidgetManager.getInstance(context)

        val quickIds = mgr.getAppWidgetIds(ComponentName(context, HelpyQuickLaunchWidgetProvider::class.java))
        if (quickIds != null && quickIds.isNotEmpty()) {
            val quickIntent = Intent(context, HelpyQuickLaunchWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, quickIds)
            }
            context.sendBroadcast(quickIntent)
        }

        val rateIds = mgr.getAppWidgetIds(ComponentName(context, HelpyLiveRateWidgetProvider::class.java))
        if (rateIds != null && rateIds.isNotEmpty()) {
            val rateIntent = Intent(context, HelpyLiveRateWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, rateIds)
            }
            context.sendBroadcast(rateIntent)
        }
    }

    fun syncCurrency(context: Context, rateText: String, updatedText: String = "Tap to convert") {
        syncWidgetState(
            context = context,
            currencyRate = rateText,
            currencyUpdated = updatedText,
        )
    }

    fun syncTheme(context: Context, bgHex: String, primaryHex: String, textHex: String) {
        syncWidgetState(
            context = context,
            bgHex = bgHex,
            primaryHex = primaryHex,
            textHex = textHex,
        )
    }
}
