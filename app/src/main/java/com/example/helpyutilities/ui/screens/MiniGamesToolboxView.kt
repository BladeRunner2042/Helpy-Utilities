package com.example.helpyutilities.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.helpyutilities.data.AppPreferences
import com.example.helpyutilities.ui.components.FrostedGlassCard
import com.example.helpyutilities.ui.theme.DarkBg
import com.example.helpyutilities.ui.theme.DarkBorder
import com.example.helpyutilities.ui.theme.ElectricCyan
import com.example.helpyutilities.ui.theme.EmeraldLime
import com.example.helpyutilities.ui.theme.NeonRose
import com.example.helpyutilities.ui.theme.SunsetGold
import com.example.helpyutilities.ui.theme.TextMuted
import com.example.helpyutilities.ui.theme.TextPrimary
import com.example.helpyutilities.ui.theme.TextSecondary
import com.example.helpyutilities.util.HapticFeedbackHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Built-in Brain & Mini-Games Toolbox:
 * 1. 2048 Block Puzzle (Classic, smooth 4x4 sliding puzzle)
 * 2. Reflex / Reaction Speed Test (Millisecond precision reaction benchmark)
 * 3. Memory Sequence (Simon-style cognitive memory grid)
 * 4. Tic-Tac-Toe (vs AI or 2 Player)
 */
@Composable
fun MiniGamesToolboxView() {
    val context = LocalContext.current
    var selectedGameTab by remember { mutableIntStateOf(0) }
    val gameTabs = listOf("2048 Puzzle", "Reflex Test", "Memory Matrix", "Tic-Tac-Toe")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tab Selector Chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(gameTabs.indices.toList()) { idx ->
                FilterChip(
                    selected = selectedGameTab == idx,
                    onClick = {
                        HapticFeedbackHelper.trigger(context)
                        selectedGameTab = idx
                    },
                    label = { Text(gameTabs[idx], fontWeight = FontWeight.Bold, maxLines = 1) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ElectricCyan,
                        selectedLabelColor = DarkBg,
                        containerColor = Color(0x331E293B),
                        labelColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(14.dp)
                )
            }
        }

        when (selectedGameTab) {
            0 -> Game2048View()
            1 -> ReflexSpeedTestView()
            2 -> MemoryMatrixGameView()
            3 -> TicTacToeGameView()
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// -------------------------------------------------------------------------------------------------
// 1. 2048 SLIDING BLOCK PUZZLE
// -------------------------------------------------------------------------------------------------
@Composable
fun Game2048View() {
    val context = LocalContext.current
    var score by remember { mutableIntStateOf(0) }
    var bestScore by remember { mutableIntStateOf(0) }
    var grid by remember {
        mutableStateOf(
            Array(4) { IntArray(4) }.apply {
                // Initialize with two random tiles
                val r1 = Random.nextInt(4); val c1 = Random.nextInt(4)
                var r2 = Random.nextInt(4); var c2 = Random.nextInt(4)
                while (r1 == r2 && c1 == c2) { r2 = Random.nextInt(4); c2 = Random.nextInt(4) }
                this[r1][c1] = 2; this[r2][c2] = 2
            }
        )
    }

    fun restartGame() {
        score = 0
        val newGrid = Array(4) { IntArray(4) }
        val r1 = Random.nextInt(4); val c1 = Random.nextInt(4)
        var r2 = Random.nextInt(4); var c2 = Random.nextInt(4)
        while (r1 == r2 && c1 == c2) { r2 = Random.nextInt(4); c2 = Random.nextInt(4) }
        newGrid[r1][c1] = 2; newGrid[r2][c2] = 2
        grid = newGrid
    }

    fun spawnTile(g: Array<IntArray>) {
        val emptySpots = mutableListOf<Pair<Int, Int>>()
        for (r in 0..3) {
            for (c in 0..3) {
                if (g[r][c] == 0) emptySpots.add(r to c)
            }
        }
        if (emptySpots.isNotEmpty()) {
            val (r, c) = emptySpots.random()
            g[r][c] = if (Random.nextFloat() < 0.9f) 2 else 4
        }
    }

    fun slideRow(row: IntArray): Pair<IntArray, Int> {
        val filtered = row.filter { it != 0 }.toMutableList()
        var gainedScore = 0
        var i = 0
        val merged = mutableListOf<Int>()
        while (i < filtered.size) {
            if (i + 1 < filtered.size && filtered[i] == filtered[i + 1]) {
                val sum = filtered[i] * 2
                merged.add(sum)
                gainedScore += sum
                i += 2
            } else {
                merged.add(filtered[i])
                i++
            }
        }
        while (merged.size < 4) merged.add(0)
        return merged.toIntArray() to gainedScore
    }

    fun move(direction: Int) {
        // 0: Left, 1: Right, 2: Up, 3: Down
        HapticFeedbackHelper.trigger(context)
        val copy = Array(4) { r -> grid[r].clone() }
        var addedScore = 0
        var changed = false

        when (direction) {
            0 -> { // Left
                for (r in 0..3) {
                    val (res, pts) = slideRow(copy[r])
                    addedScore += pts
                    if (!res.contentEquals(copy[r])) changed = true
                    copy[r] = res
                }
            }
            1 -> { // Right
                for (r in 0..3) {
                    val reversed = copy[r].reversedArray()
                    val (res, pts) = slideRow(reversed)
                    val back = res.reversedArray()
                    addedScore += pts
                    if (!back.contentEquals(copy[r])) changed = true
                    copy[r] = back
                }
            }
            2 -> { // Up
                for (c in 0..3) {
                    val col = IntArray(4) { r -> copy[r][c] }
                    val (res, pts) = slideRow(col)
                    addedScore += pts
                    for (r in 0..3) {
                        if (copy[r][c] != res[r]) changed = true
                        copy[r][c] = res[r]
                    }
                }
            }
            3 -> { // Down
                for (c in 0..3) {
                    val col = IntArray(4) { r -> copy[3 - r][c] }
                    val (res, pts) = slideRow(col)
                    addedScore += pts
                    for (r in 0..3) {
                        if (copy[3 - r][c] != res[r]) changed = true
                        copy[3 - r][c] = res[r]
                    }
                }
            }
        }

        if (changed) {
            spawnTile(copy)
            score += addedScore
            if (score > bestScore) bestScore = score
            grid = copy
        }
    }

    FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("2048 Block Puzzle", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Join identical numbers to reach 2048!", fontSize = 12.sp, color = TextMuted)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1E293B))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("SCORE", fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                            Text("$score", fontSize = 14.sp, color = EmeraldLime, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1E293B))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("BEST", fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                            Text("$bestScore", fontSize = 14.sp, color = SunsetGold, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }

            // 4x4 Grid Board
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF0F172A))
                    .border(2.dp, DarkBorder, RoundedCornerShape(18.dp))
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (r in 0..3) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (c in 0..3) {
                                val v = grid[r][c]
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(get2048Color(v)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (v > 0) {
                                        Text(
                                            text = "$v",
                                            fontSize = if (v >= 1024) 14.sp else if (v >= 128) 16.sp else 19.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (v in 2..4) Color(0xFF0F172A) else Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // D-Pad Directional Controls
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = { move(2) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(width = 72.dp, height = 40.dp)
                ) {
                    Text("▲", fontSize = 16.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    Button(
                        onClick = { move(0) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(width = 72.dp, height = 40.dp)
                    ) {
                        Text("◀", fontSize = 16.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { move(3) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(width = 72.dp, height = 40.dp)
                    ) {
                        Text("▼", fontSize = 16.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { move(1) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(width = 72.dp, height = 40.dp)
                    ) {
                        Text("▶", fontSize = 16.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Button(
                onClick = {
                    HapticFeedbackHelper.trigger(context)
                    restartGame()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0x331E293B)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Restart 2048", color = TextPrimary, fontSize = 13.sp)
            }
        }
    }
}

private fun get2048Color(v: Int): Color {
    return when (v) {
        0 -> Color(0x221E293B)
        2 -> Color(0xFFE2E8F0)
        4 -> Color(0xFFCBD5E1)
        8 -> Color(0xFFFBBF24)
        16 -> Color(0xFFF59E0B)
        32 -> Color(0xFFFB923C)
        64 -> Color(0xFFEA580C)
        128 -> Color(0xFF38BDF8)
        256 -> Color(0xFF0284C7)
        512 -> Color(0xFF10B981)
        1024 -> Color(0xFF059669)
        2048 -> Color(0xFF8B5CF6)
        else -> Color(0xFFEC4899)
    }
}

// -------------------------------------------------------------------------------------------------
// 2. REFLEX / REACTION SPEED TEST
// -------------------------------------------------------------------------------------------------
@Composable
fun ReflexSpeedTestView() {
    val context = LocalContext.current
    var state by remember { mutableStateOf("IDLE") } // IDLE, WAITING, READY, FINISHED, TOO_EARLY
    var startTime by remember { mutableLongStateOf(0L) }
    var reactionTimeMs by remember { mutableLongStateOf(0L) }
    var bestTimeMs by remember { mutableLongStateOf(0L) }

    LaunchedEffect(state) {
        if (state == "WAITING") {
            val randomDelay = Random.nextLong(1500, 4500)
            delay(randomDelay)
            if (state == "WAITING") {
                state = "READY"
                startTime = System.currentTimeMillis()
                HapticFeedbackHelper.trigger(context)
            }
        }
    }

    FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Human Reflex & Reaction Test", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("Tap as soon as the screen flashes green!", fontSize = 12.sp, color = TextMuted)
            }

            // Big Interactive Reaction Box
            val boxBg = when (state) {
                "WAITING" -> Color(0xFFE11D48) // Red (Wait)
                "READY" -> Color(0xFF10B981) // Green (Tap now!)
                "TOO_EARLY" -> Color(0xFFF59E0B) // Amber
                "FINISHED" -> Color(0xFF0284C7) // Blue
                else -> Color(0xFF1E293B) // Dark Idle
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(boxBg)
                    .border(2.dp, Color(0x33FFFFFF), RoundedCornerShape(22.dp))
                    .clickable {
                        when (state) {
                            "IDLE" -> {
                                HapticFeedbackHelper.trigger(context)
                                state = "WAITING"
                            }
                            "WAITING" -> {
                                HapticFeedbackHelper.trigger(context)
                                state = "TOO_EARLY"
                            }
                            "READY" -> {
                                reactionTimeMs = System.currentTimeMillis() - startTime
                                HapticFeedbackHelper.trigger(context)
                                if (bestTimeMs == 0L || reactionTimeMs < bestTimeMs) bestTimeMs = reactionTimeMs
                                state = "FINISHED"
                            }
                            "FINISHED", "TOO_EARLY" -> {
                                HapticFeedbackHelper.trigger(context)
                                state = "WAITING"
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    when (state) {
                        "IDLE" -> {
                            Text("⚡", fontSize = 42.sp)
                            Text("Tap to Start Test", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                            Text("Wait for the green signal", fontSize = 13.sp, color = TextMuted)
                        }
                        "WAITING" -> {
                            Text("⏳", fontSize = 42.sp)
                            Text("WAIT FOR GREEN...", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                            Text("Don't tap yet!", fontSize = 13.sp, color = Color(0xCCFFFFFF))
                        }
                        "READY" -> {
                            Text("🎯", fontSize = 42.sp)
                            Text("TAP NOW!!!", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        }
                        "TOO_EARLY" -> {
                            Text("⚠️", fontSize = 42.sp)
                            Text("Too early!", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                            Text("Tap anywhere to try again", fontSize = 13.sp, color = Color(0xCCFFFFFF))
                        }
                        "FINISHED" -> {
                            Text("⏱️", fontSize = 38.sp)
                            Text("${reactionTimeMs} ms", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                            Text(
                                text = when {
                                    reactionTimeMs < 200 -> "Legendary speed! 🚀"
                                    reactionTimeMs < 250 -> "Faster than average! ⚡"
                                    reactionTimeMs < 350 -> "Good human reaction! 👍"
                                    else -> "Keep practicing! ☕"
                                },
                                fontSize = 13.sp,
                                color = Color(0xEEFFFFFF)
                            )
                        }
                    }
                }
            }

            if (bestTimeMs > 0) {
                Text(
                    text = "Personal Best: $bestTimeMs ms",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldLime
                )
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// 3. MEMORY MATRIX (SIMON COGNITIVE GAME)
// -------------------------------------------------------------------------------------------------
@Composable
fun MemoryMatrixGameView() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var level by remember { mutableIntStateOf(1) }
    var bestLevel by remember { mutableIntStateOf(1) }
    var activeFlashIndex by remember { mutableIntStateOf(-1) }
    val sequence = remember { mutableStateListOf<Int>() }
    var playerTurn by remember { mutableStateOf(false) }
    var playerStep by remember { mutableIntStateOf(0) }
    var gameOver by remember { mutableStateOf(false) }

    fun playRound() {
        coroutineScope.launch {
            playerTurn = false
            playerStep = 0
            gameOver = false
            sequence.add(Random.nextInt(4))
            delay(500)
            for (tile in sequence) {
                activeFlashIndex = tile
                HapticFeedbackHelper.trigger(context)
                delay(400)
                activeFlashIndex = -1
                delay(200)
            }
            playerTurn = true
        }
    }

    fun startNewGame() {
        level = 1
        sequence.clear()
        playRound()
    }

    FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Memory Matrix", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Memorize the flashing sequence", fontSize = 12.sp, color = TextMuted)
                }
                Text("Level: $level", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SunsetGold)
            }

            // 2x2 Grid Tiles
            val tileColors = listOf(
                Color(0xFF3B82F6), // Blue
                Color(0xFF10B981), // Green
                Color(0xFFF59E0B), // Amber
                Color(0xFFEF4444)  // Red
            )

            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF0F172A))
                    .padding(10.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    for (row in 0..1) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            for (col in 0..1) {
                                val idx = row * 2 + col
                                val isFlashing = activeFlashIndex == idx
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (isFlashing) Color.White else tileColors[idx].copy(alpha = 0.75f))
                                        .border(2.dp, if (isFlashing) Color.White else tileColors[idx], RoundedCornerShape(16.dp))
                                        .clickable(enabled = playerTurn) {
                                            HapticFeedbackHelper.trigger(context)
                                            if (idx == sequence[playerStep]) {
                                                playerStep++
                                                if (playerStep >= sequence.size) {
                                                    level++
                                                    if (level > bestLevel) bestLevel = level
                                                    playRound()
                                                }
                                            } else {
                                                gameOver = true
                                                playerTurn = false
                                            }
                                        }
                                )
                            }
                        }
                    }
                }
            }

            if (gameOver) {
                Text("Game Over! Highest Level: $bestLevel", color = NeonRose, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Button(
                onClick = {
                    HapticFeedbackHelper.trigger(context)
                    startNewGame()
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(if (sequence.isEmpty()) "Start Game ▶" else "Restart", color = DarkBg, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// 4. TIC-TAC-TOE (SMART AI & 2 PLAYER)
// -------------------------------------------------------------------------------------------------
@Composable
fun TicTacToeGameView() {
    val context = LocalContext.current
    var board by remember { mutableStateOf(Array(3) { Array(3) { "" } }) }
    var isXTurn by remember { mutableStateOf(true) }
    var vsAI by remember { mutableStateOf(true) }
    var winner by remember { mutableStateOf<String?>(null) } // "X", "O", "TIE", null

    fun checkWinner(b: Array<Array<String>>): String? {
        for (i in 0..2) {
            if (b[i][0].isNotEmpty() && b[i][0] == b[i][1] && b[i][1] == b[i][2]) return b[i][0]
            if (b[0][i].isNotEmpty() && b[0][i] == b[1][i] && b[1][i] == b[2][i]) return b[0][i]
        }
        if (b[0][0].isNotEmpty() && b[0][0] == b[1][1] && b[1][1] == b[2][2]) return b[0][0]
        if (b[0][2].isNotEmpty() && b[0][2] == b[1][1] && b[1][1] == b[2][0]) return b[0][2]
        if (b.all { row -> row.all { it.isNotEmpty() } }) return "TIE"
        return null
    }

    fun makeAIMove(b: Array<Array<String>>) {
        val emptyCells = mutableListOf<Pair<Int, Int>>()
        for (r in 0..2) {
            for (c in 0..2) {
                if (b[r][c].isEmpty()) emptyCells.add(r to c)
            }
        }
        if (emptyCells.isNotEmpty()) {
            val (r, c) = emptyCells.random()
            b[r][c] = "O"
            winner = checkWinner(b)
            isXTurn = true
        }
    }

    fun handleCellClick(r: Int, c: Int) {
        if (board[r][c].isNotEmpty() || winner != null) return
        HapticFeedbackHelper.trigger(context)

        val newBoard = Array(3) { row -> board[row].clone() }
        newBoard[r][c] = if (isXTurn) "X" else "O"
        winner = checkWinner(newBoard)

        if (winner == null && vsAI && isXTurn) {
            isXTurn = false
            board = newBoard
            makeAIMove(newBoard)
        } else {
            isXTurn = !isXTurn
            board = newBoard
        }
    }

    FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Tic-Tac-Toe", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(if (vsAI) "Playing vs Smart AI" else "2 Players Local", fontSize = 12.sp, color = TextMuted)
                }
                Button(
                    onClick = {
                        HapticFeedbackHelper.trigger(context)
                        vsAI = !vsAI
                        board = Array(3) { Array(3) { "" } }
                        winner = null
                        isXTurn = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x331E293B)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(if (vsAI) "Vs AI 🤖" else "2-Player 👥", fontSize = 12.sp, color = TextPrimary)
                }
            }

            // 3x3 Grid
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF0F172A))
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (r in 0..2) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (c in 0..2) {
                                val cell = board[r][c]
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color(0xFF1E293B))
                                        .clickable { handleCellClick(r, c) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = cell,
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (cell == "X") ElectricCyan else SunsetGold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (winner != null) {
                Text(
                    text = when (winner) {
                        "TIE" -> "It's a Draw! 🤝"
                        "X" -> "Player X Wins! 🏆"
                        else -> if (vsAI) "AI Wins! 🤖" else "Player O Wins! 🏆"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (winner == "X") ElectricCyan else SunsetGold
                )
            }

            Button(
                onClick = {
                    HapticFeedbackHelper.trigger(context)
                    board = Array(3) { Array(3) { "" } }
                    winner = null
                    isXTurn = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0x331E293B)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Restart Game", color = TextPrimary, fontSize = 13.sp)
            }
        }
    }
}
