package com.example.helpyutilities.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.helpyutilities.MainActivity

/**
 * 1. Helpy Quick Hub Widget: 4-Module Quick Launcher for Home Screen
 */
class HelpyGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceWidgetContent(context)
        }
    }

    @Composable
    private fun GlanceWidgetContent(context: Context) {
        val torchIntent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("helpy://tool/torch")).apply {
            setClass(context, MainActivity::class.java)
            putExtra("EXTRA_TAB", 1)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val calcIntent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("helpy://tool/calculator")).apply {
            setClass(context, MainActivity::class.java)
            putExtra("EXTRA_TAB", 2)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val transIntent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("helpy://tool/translate")).apply {
            setClass(context, MainActivity::class.java)
            putExtra("EXTRA_TAB", 4)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val speedIntent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("helpy://tool/speedtest")).apply {
            setClass(context, MainActivity::class.java)
            putExtra("EXTRA_TAB", 1)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val homeIntent = Intent(context, MainActivity::class.java).apply {
            putExtra("EXTRA_TAB", 0)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .cornerRadius(24.dp)
                .background(Color(0xFF0F172A))
                .padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalAlignment = Alignment.Start,
        ) {
            // Header Bar
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .clickable(actionStartActivity(homeIntent)),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Helpy Hub",
                    style = TextStyle(
                        color = ColorProvider(day = Color(0xFF10B981), night = Color(0xFF10B981)),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
                Text(
                    text = "4x2 Quick Bento",
                    style = TextStyle(
                        color = ColorProvider(day = Color(0xFF94A3B8), night = Color(0xFF94A3B8)),
                        fontSize = 11.sp,
                    ),
                )
            }

            Spacer(modifier = GlanceModifier.height(10.dp))

            // Row 1: Torch & Calculator
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = GlanceModifier
                        .defaultWeight()
                        .cornerRadius(16.dp)
                        .background(Color(0xFF1E293B))
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        .clickable(actionStartActivity(torchIntent)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Torch",
                        style = TextStyle(
                            color = ColorProvider(day = Color(0xFFFBBF24), night = Color(0xFFFBBF24)),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                }

                Spacer(modifier = GlanceModifier.width(8.dp))

                Box(
                    modifier = GlanceModifier
                        .defaultWeight()
                        .cornerRadius(16.dp)
                        .background(Color(0xFF1E293B))
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        .clickable(actionStartActivity(calcIntent)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Calculator",
                        style = TextStyle(
                            color = ColorProvider(day = Color(0xFF38BDF8), night = Color(0xFF38BDF8)),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                }
            }

            Spacer(modifier = GlanceModifier.height(8.dp))

            // Row 2: Translate & Speed Test
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = GlanceModifier
                        .defaultWeight()
                        .cornerRadius(16.dp)
                        .background(Color(0xFF1E293B))
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        .clickable(actionStartActivity(transIntent)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Translate",
                        style = TextStyle(
                            color = ColorProvider(day = Color(0xFF34D399), night = Color(0xFF34D399)),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                }

                Spacer(modifier = GlanceModifier.width(8.dp))

                Box(
                    modifier = GlanceModifier
                        .defaultWeight()
                        .cornerRadius(16.dp)
                        .background(Color(0xFF1E293B))
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        .clickable(actionStartActivity(speedIntent)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Speed Test",
                        style = TextStyle(
                            color = ColorProvider(day = Color(0xFF06B6D4), night = Color(0xFF06B6D4)),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                }
            }
        }
    }
}

class HelpyGlanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = HelpyGlanceWidget()
}

/**
 * 2. Helpy Live Currency & Market Rates Widget for Home Screen
 */
class HelpyCurrencyGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            CurrencyGlanceContent(context)
        }
    }

    @Composable
    private fun CurrencyGlanceContent(context: Context) {
        val currIntent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("helpy://tool/currency")).apply {
            setClass(context, MainActivity::class.java)
            putExtra("EXTRA_TAB", 3)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .cornerRadius(24.dp)
                .background(Color(0xFF0F172A))
                .padding(14.dp)
                .clickable(actionStartActivity(currIntent)),
            verticalAlignment = Alignment.Top,
            horizontalAlignment = Alignment.Start,
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Live Forex & Rates",
                    style = TextStyle(
                        color = ColorProvider(day = Color(0xFFFBBF24), night = Color(0xFFFBBF24)),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
                Text(
                    text = "Syncs Live ⚡",
                    style = TextStyle(
                        color = ColorProvider(day = Color(0xFF94A3B8), night = Color(0xFF94A3B8)),
                        fontSize = 10.sp,
                    ),
                )
            }

            Spacer(modifier = GlanceModifier.height(8.dp))

            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        text = "USD / ZAR",
                        style = TextStyle(color = ColorProvider(day = Color(0xFF94A3B8), night = Color(0xFF94A3B8)), fontSize = 11.sp),
                    )
                    Text(
                        text = "18.45 R",
                        style = TextStyle(color = ColorProvider(day = Color(0xFF38BDF8), night = Color(0xFF38BDF8)), fontSize = 15.sp, fontWeight = FontWeight.Bold),
                    )
                }

                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        text = "USD / EUR",
                        style = TextStyle(color = ColorProvider(day = Color(0xFF94A3B8), night = Color(0xFF94A3B8)), fontSize = 11.sp),
                    )
                    Text(
                        text = "0.92 €",
                        style = TextStyle(color = ColorProvider(day = Color(0xFF34D399), night = Color(0xFF34D399)), fontSize = 15.sp, fontWeight = FontWeight.Bold),
                    )
                }

                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        text = "USD / GBP",
                        style = TextStyle(color = ColorProvider(day = Color(0xFF94A3B8), night = Color(0xFF94A3B8)), fontSize = 11.sp),
                    )
                    Text(
                        text = "0.78 £",
                        style = TextStyle(color = ColorProvider(day = Color(0xFFFBBF24), night = Color(0xFFFBBF24)), fontSize = 15.sp, fontWeight = FontWeight.Bold),
                    )
                }
            }
        }
    }
}

class HelpyCurrencyGlanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = HelpyCurrencyGlanceWidget()
}

/**
 * 3. Helpy Quick Tools Mini-Widget for Home Screen
 */
class HelpyQuickToolsGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            QuickToolsGlanceContent(context)
        }
    }

    @Composable
    private fun QuickToolsGlanceContent(context: Context) {
        val toolIntent = Intent(context, MainActivity::class.java).apply {
            putExtra("EXTRA_TAB", 1)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .cornerRadius(20.dp)
                .background(Color(0xFF0F172A))
                .padding(12.dp)
                .clickable(actionStartActivity(toolIntent)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "⚡ Flash & Tools",
                style = TextStyle(
                    color = ColorProvider(day = Color(0xFF38BDF8), night = Color(0xFF38BDF8)),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
            Spacer(modifier = GlanceModifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "🔦 Torch",
                    style = TextStyle(color = ColorProvider(day = Color(0xFFFBBF24), night = Color(0xFFFBBF24)), fontSize = 11.sp, fontWeight = FontWeight.Bold),
                )
                Spacer(modifier = GlanceModifier.width(8.dp))
                Text(
                    text = "📐 Level",
                    style = TextStyle(color = ColorProvider(day = Color(0xFF34D399), night = Color(0xFF34D399)), fontSize = 11.sp, fontWeight = FontWeight.Bold),
                )
                Spacer(modifier = GlanceModifier.width(8.dp))
                Text(
                    text = "🎲 Dice",
                    style = TextStyle(color = ColorProvider(day = Color(0xFFF43F5E), night = Color(0xFFF43F5E)), fontSize = 11.sp, fontWeight = FontWeight.Bold),
                )
            }
        }
    }
}

class HelpyQuickToolsGlanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = HelpyQuickToolsGlanceWidget()
}
