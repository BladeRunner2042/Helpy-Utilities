package com.example.helpyutilities.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.helpyutilities.ui.components.AnimatedMeshBackground
import com.example.helpyutilities.ui.components.FrostedGlassCard
import com.example.helpyutilities.ui.components.HelpyNavItem
import com.example.helpyutilities.ui.components.MaterialCompactPillNavBar
import com.example.helpyutilities.ui.screens.CalculatorScreen
import com.example.helpyutilities.ui.screens.CurrencyScreen
import com.example.helpyutilities.ui.screens.HomeScreen
import com.example.helpyutilities.ui.screens.MathToolsModal
import com.example.helpyutilities.ui.screens.SettingsScreen
import com.example.helpyutilities.ui.screens.ToolboxScreen
import com.example.helpyutilities.ui.screens.TranslateScreen
import com.example.helpyutilities.util.HapticFeedbackHelper
import kotlinx.coroutines.delay

data class QuickSearchTool(
    val title: String,
    val subtitle: String,
    val tabIndex: Int,
    val toolModalId: Int = 0,
    val icon: ImageVector,
    val color: Color,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    initialTab: Int = 0,
    prefilledQuery: String? = null,
) {
    val context = LocalContext.current
    val currentTheme = com.example.helpyutilities.ui.theme.LocalAppTheme.current

    var selectedBottomTab by remember { mutableIntStateOf(initialTab) }
    var previousTab by remember { mutableIntStateOf(0) }
    var showSearchSheet by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var activeGlobalModalId by remember { mutableIntStateOf(0) }

    // Auto‑hide navigation pill state
    val navBarVisibilityState = remember { mutableStateOf(true) }
    var lastInteractionTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // Idle timer
    LaunchedEffect(Unit) {
        while (true) {
            delay(200L)
            if (System.currentTimeMillis() - lastInteractionTime > 5000 && navBarVisibilityState.value) {
                navBarVisibilityState.value = false
            }
        }
    }

    // Animated pill offset
    val pillOffsetY by animateDpAsState(
        targetValue = if (navBarVisibilityState.value) 0.dp else 80.dp,
        animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
        label = "pillOffsetAnimation",
    )

    // Nested scroll connection
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                if (available.y < 0) {
                    navBarVisibilityState.value = false
                } else if (available.y > 0) {
                    navBarVisibilityState.value = true
                }
                lastInteractionTime = System.currentTimeMillis()
                return Offset.Zero
            }
        }
    }

    val modalSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Predictive Back Handling: If in modal or search sheet or non-home tab, dismiss or navigate back smoothly
    BackHandler(enabled = showSearchSheet || activeGlobalModalId != 0 || selectedBottomTab != 0) {
        if (showSearchSheet) {
            showSearchSheet = false
        } else if (activeGlobalModalId != 0) {
            activeGlobalModalId = 0
        } else if (selectedBottomTab != 0) {
            selectedBottomTab = if (selectedBottomTab == 5) previousTab else 0
        }
    }

    val allTools = remember(currentTheme) {
        listOf(
            QuickSearchTool("Home Dashboard", "Overview, vitals & quick launch cards", 0, 0, Icons.Default.Home, currentTheme.primary),
            QuickSearchTool("Password Generator", "Create high-entropy passwords & PINs", 1, 0, Icons.Default.Widgets, currentTheme.primary),
            QuickSearchTool("Scientific Calculator", "Full expressions, powers, trig functions", 2, 0, Icons.Default.Calculate, currentTheme.secondary),
            QuickSearchTool("Unit Converter", "Length, weight, temperature, data & more", 2, 1, Icons.Default.Calculate, currentTheme.primary),
            QuickSearchTool("Currency Exchange", "Live rates for 160+ world currencies", 3, 0, Icons.Default.CurrencyExchange, currentTheme.secondary),
            QuickSearchTool("Universal Translator", "Text, Camera OCR & offline 160+ languages", 4, 0, Icons.Default.Translate, currentTheme.primary),
            QuickSearchTool("Bubble Level", "Real device accelerometer tilt inclinometer", 1, 0, Icons.Default.Widgets, currentTheme.secondary),
            QuickSearchTool("Loan & Mortgage EMI", "Calculate monthly loan interest & principal", 2, 2, Icons.Default.Calculate, currentTheme.primary),
            QuickSearchTool("Tip & Bill Split", "Split dining bills and tips per person", 2, 3, Icons.Default.Calculate, currentTheme.secondary),
            QuickSearchTool("Discount & Sales Tax", "Calculate net savings and retail taxes", 2, 4, Icons.Default.Calculate, currentTheme.secondary),
            QuickSearchTool("BMI Health Gauge", "Body mass index & ideal weight metrics", 2, 5, Icons.Default.Calculate, currentTheme.primary),
            QuickSearchTool("Date & Age Counter", "Exact age in years, months, days & countdown", 2, 6, Icons.Default.Calculate, currentTheme.secondary),
            QuickSearchTool("Base64 & Hash", "MD5, SHA-1, SHA-256 and Base64 encoder", 1, 0, Icons.Default.Widgets, currentTheme.secondary),
            QuickSearchTool("URL Tracking Stripper", "Clean tracking params & hashtags", 1, 0, Icons.Default.Widgets, currentTheme.primary),
            QuickSearchTool("Network Speed Test", "Real-time Ping, Jitter & Wi-Fi Gateway stats", 1, 0, Icons.Default.Widgets, currentTheme.primary),
            QuickSearchTool("Morse Code Torch", "SOS and custom text light transmitter", 1, 0, Icons.Default.Widgets, currentTheme.secondary),
            QuickSearchTool("World Time Scrubber", "Interactive 15-minute global clocks", 1, 0, Icons.Default.Widgets, currentTheme.primary),
            QuickSearchTool("Device Hardware & CPU", "CPU cores, thermals, GPU & RAM stats", 1, 0, Icons.Default.Widgets, currentTheme.primary),
            QuickSearchTool("App Settings & Themes", "Customize theme, haptics and cache", 5, 0, Icons.Default.Settings, currentTheme.secondary),
        )
    }

    val filteredTools = remember(searchQuery, allTools) {
        if (searchQuery.isBlank()) allTools else {
            allTools.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.subtitle.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures {
                    lastInteractionTime = System.currentTimeMillis()
                    navBarVisibilityState.value = true
                }
            }
            .nestedScroll(nestedScrollConnection),
    ) {
        AnimatedMeshBackground(enabled = true)

        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            topBar = {
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .height(52.dp)
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (selectedBottomTab == 5) {
                                IconButton(
                                    onClick = {
                                        HapticFeedbackHelper.trigger(context)
                                        selectedBottomTab = previousTab
                                    },
                                    modifier = Modifier.size(38.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = currentTheme.textPrimary,
                                    )
                                }
                                Text(
                                    text = "Settings",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 19.sp,
                                    color = currentTheme.textPrimary,
                                    letterSpacing = (-0.4).sp,
                                )
                            } else {
                                Text(
                                    text = "Helpy",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = currentTheme.textPrimary,
                                    letterSpacing = (-0.4).sp,
                                )
                            }
                        }

                        if (selectedBottomTab != 5) {
                            IconButton(
                                onClick = {
                                    HapticFeedbackHelper.trigger(context)
                                    previousTab = selectedBottomTab
                                    selectedBottomTab = 5
                                },
                                modifier = Modifier.size(38.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = currentTheme.textPrimary,
                                    modifier = Modifier.size(22.dp),
                                )
                            }
                        }
                    }
                }
            },
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding()),
            ) {
                AnimatedContent(
                    targetState = selectedBottomTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "tabTransition",
                ) { targetTab ->
                    when (targetTab) {
                        0 -> HomeScreen(
                            onNavigateToTab = { tab ->
                                selectedBottomTab = tab
                            },
                            onOpenSettings = {
                                previousTab = selectedBottomTab
                                selectedBottomTab = 5
                            },
                            onOpenSearch = {
                                showSearchSheet = true
                            },
                        )
                        1 -> ToolboxScreen()
                        2 -> CalculatorScreen(initialExpression = if (selectedBottomTab == 2) prefilledQuery else null)
                        3 -> CurrencyScreen()
                        4 -> TranslateScreen(initialText = if (selectedBottomTab == 4) prefilledQuery else null)
                        5 -> SettingsScreen()
                    }
                }
            }
        }

        // Floating Bottom Navigation Pill
        val navItems = remember {
            listOf(
                HelpyNavItem(0, Icons.Default.Home, "Home"),
                HelpyNavItem(1, Icons.Default.Widgets, "Toolbox"),
                HelpyNavItem(2, Icons.Default.Calculate, "Calculator"),
                HelpyNavItem(3, Icons.Default.CurrencyExchange, "Currency"),
                HelpyNavItem(4, Icons.Default.Translate, "Translate"),
            )
        }

        MaterialCompactPillNavBar(
            selectedIndex = selectedBottomTab.coerceIn(0, 4),
            items = navItems,
            onTabSelected = { selectedBottomTab = it },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .graphicsLayer { translationY = pillOffsetY.toPx() },
        )

        // Quick Global Search Sheet
        if (showSearchSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSearchSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = currentTheme.cardBackground,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Text(
                        text = "Instant Utility Launcher",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = currentTheme.textPrimary,
                    )

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Type tool name (e.g. loan, unit, pass, coin)...", color = currentTheme.textSecondary) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = currentTheme.primary) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = currentTheme.textSecondary)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = currentTheme.primary,
                            unfocusedBorderColor = currentTheme.borderColor,
                        ),
                    )

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(filteredTools) { tool ->
                            FrostedGlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        HapticFeedbackHelper.trigger(context)
                                        showSearchSheet = false
                                        selectedBottomTab = tool.tabIndex
                                        if (tool.toolModalId != 0) {
                                            activeGlobalModalId = tool.toolModalId
                                        }
                                    },
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(tool.color.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            imageVector = tool.icon,
                                            contentDescription = null,
                                            tint = tool.color,
                                            modifier = Modifier.size(22.dp),
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(tool.title, fontWeight = FontWeight.Bold, color = currentTheme.textPrimary, fontSize = 15.sp)
                                        Text(tool.subtitle, fontSize = 12.sp, color = currentTheme.textSecondary, maxLines = 1)
                                    }

                                    Text("Open →", color = tool.color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Secondary Global Modal Launcher if launched from search
        if (activeGlobalModalId != 0) {
            MathToolsModal(
                activeTool = activeGlobalModalId,
                sheetState = modalSheetState,
                onDismiss = { activeGlobalModalId = 0 },
            )
        }
    }
}
