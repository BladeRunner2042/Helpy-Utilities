package com.example.helpyutilities.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.helpyutilities.data.AppPreferences
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.helpyutilities.ui.components.FrostedGlassCard
import com.example.helpyutilities.ui.theme.DarkBg
import com.example.helpyutilities.ui.theme.DarkBorder
import com.example.helpyutilities.ui.theme.DarkCardBg
import com.example.helpyutilities.ui.theme.ElectricCyan
import com.example.helpyutilities.ui.theme.EmeraldLime
import com.example.helpyutilities.ui.theme.NeonRose
import com.example.helpyutilities.ui.theme.SunsetGold
import com.example.helpyutilities.ui.theme.TextMuted
import com.example.helpyutilities.ui.theme.TextPrimary
import com.example.helpyutilities.ui.theme.TextSecondary
import com.example.helpyutilities.util.UnitConverter
import java.util.Locale
import kotlin.math.pow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MathToolsModal(
    activeTool: Int,
    sheetState: SheetState,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0F172A),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(Color(0x4D94A3B8))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (activeTool) {
                        1 -> "Universal Unit Converter"
                        2 -> "Loan & Mortgage Calculator"
                        3 -> "Tip & Split Bill"
                        4 -> "Discount & Sales Tax"
                        5 -> "BMI & Health Check"
                        6 -> "Date & Age Counter"
                        7 -> "Percentage Calculator"
                        else -> "Smart Calculator Tool"
                    },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }

            when (activeTool) {
                1 -> UnitConverterModalView()
                2 -> LoanCalculatorModalView()
                3 -> TipCalculatorModalView()
                4 -> DiscountTaxModalView()
                5 -> BmiCalculatorModalView()
                6 -> AgeCalculatorModalView()
                7 -> PercentageCalculatorModalView()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitConverterModalView() {
    val savedCategoryName by AppPreferences.workspaceConverterCategory.collectAsState()
    val initialCat = remember(savedCategoryName) {
        UnitConverter.Category.values().firstOrNull { it.name.equals(savedCategoryName, ignoreCase = true) || it.displayName.equals(savedCategoryName, ignoreCase = true) }
            ?: UnitConverter.Category.LENGTH
    }
    var selectedCategory by remember(initialCat) { mutableStateOf(initialCat) }
    val units = remember(selectedCategory) { UnitConverter.getUnitsForCategory(selectedCategory) }
    var fromUnit by remember(selectedCategory) { mutableStateOf(units[0]) }
    var toUnit by remember(selectedCategory) { mutableStateOf(if (units.size > 1) units[1] else units[0]) }
    var inputValue by remember { mutableStateOf("1") }

    var showFromMenu by remember { mutableStateOf(false) }
    var showToMenu by remember { mutableStateOf(false) }

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(UnitConverter.Category.values()) { cat ->
            FilterChip(
                selected = cat == selectedCategory,
                onClick = {
                    selectedCategory = cat
                    AppPreferences.setWorkspaceConverterCategory(cat.name)
                },
                label = { Text("${cat.icon} ${cat.displayName}", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = EmeraldLime,
                    selectedLabelColor = DarkBg,
                    containerColor = Color(0x331E293B),
                    labelColor = TextPrimary
                )
            )
        }
    }

    OutlinedTextField(
        value = inputValue,
        onValueChange = { inputValue = it },
        label = { Text("Value to Convert") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = EmeraldLime,
            unfocusedBorderColor = DarkBorder,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
        )
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f)) {
            Button(
                onClick = { showFromMenu = true },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0x401E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("${fromUnit.symbol} (${fromUnit.name})", fontSize = 13.sp, color = ElectricCyan, maxLines = 1)
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = ElectricCyan)
            }
            DropdownMenu(expanded = showFromMenu, onDismissRequest = { showFromMenu = false }) {
                units.forEach { u ->
                    DropdownMenuItem(
                        text = { Text("${u.symbol} - ${u.name}") },
                        onClick = { fromUnit = u; showFromMenu = false }
                    )
                }
            }
        }

        IconButton(
            onClick = {
                val temp = fromUnit
                fromUnit = toUnit
                toUnit = temp
            },
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            Icon(Icons.Default.SwapHoriz, contentDescription = "Swap", tint = EmeraldLime)
        }

        Box(modifier = Modifier.weight(1f)) {
            Button(
                onClick = { showToMenu = true },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0x401E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("${toUnit.symbol} (${toUnit.name})", fontSize = 13.sp, color = SunsetGold, maxLines = 1)
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = SunsetGold)
            }
            DropdownMenu(expanded = showToMenu, onDismissRequest = { showToMenu = false }) {
                units.forEach { u ->
                    DropdownMenuItem(
                        text = { Text("${u.symbol} - ${u.name}") },
                        onClick = { toUnit = u; showToMenu = false }
                    )
                }
            }
        }
    }

    val inputNum = inputValue.toDoubleOrNull() ?: 0.0
    val resultVal = UnitConverter.convert(inputNum, fromUnit, toUnit, selectedCategory)
    val resultFormatted = UnitConverter.formatResult(resultVal)

    FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Equivalent Value", fontSize = 13.sp, color = TextMuted)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$resultFormatted ${toUnit.symbol}",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = EmeraldLime,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "1 ${fromUnit.symbol} = ${UnitConverter.formatResult(UnitConverter.convert(1.0, fromUnit, toUnit, selectedCategory))} ${toUnit.symbol}",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun LoanCalculatorModalView() {
    var principalText by remember { mutableStateOf("250000") }
    var rateText by remember { mutableStateOf("6.5") }
    var termYearsText by remember { mutableStateOf("30") }

    OutlinedTextField(
        value = principalText,
        onValueChange = { principalText = it },
        label = { Text("Loan Amount ($)") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldLime, unfocusedBorderColor = DarkBorder)
    )

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = rateText,
            onValueChange = { rateText = it },
            label = { Text("Interest Rate (%/yr)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldLime, unfocusedBorderColor = DarkBorder)
        )
        OutlinedTextField(
            value = termYearsText,
            onValueChange = { termYearsText = it },
            label = { Text("Term (Years)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldLime, unfocusedBorderColor = DarkBorder)
        )
    }

    val p = principalText.toDoubleOrNull() ?: 0.0
    val annualRate = rateText.toDoubleOrNull() ?: 0.0
    val years = termYearsText.toDoubleOrNull() ?: 0.0

    val monthlyRate = annualRate / 100.0 / 12.0
    val totalMonths = (years * 12).toInt().coerceAtLeast(1)

    val monthlyPayment = if (monthlyRate > 0) {
        (p * monthlyRate * (1 + monthlyRate).pow(totalMonths.toDouble())) /
                ((1 + monthlyRate).pow(totalMonths.toDouble()) - 1)
    } else {
        p / totalMonths
    }

    val totalPayback = monthlyPayment * totalMonths
    val totalInterest = (totalPayback - p).coerceAtLeast(0.0)

    FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Estimated Monthly EMI", fontSize = 13.sp, color = TextMuted)
            Text(
                text = String.format(Locale.US, "$%,.2f", monthlyPayment),
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = EmeraldLime
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Total Interest", fontSize = 12.sp, color = TextMuted)
                    Text(String.format(Locale.US, "$%,.2f", totalInterest), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SunsetGold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Total Payment", fontSize = 12.sp, color = TextMuted)
                    Text(String.format(Locale.US, "$%,.2f", totalPayback), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ElectricCyan)
                }
            }
        }
    }
}

@Composable
fun TipCalculatorModalView() {
    var billText by remember { mutableStateOf("85.00") }
    var tipPercent by remember { mutableStateOf(18) }
    var peopleCount by remember { mutableIntStateOf(2) }

    OutlinedTextField(
        value = billText,
        onValueChange = { billText = it },
        label = { Text("Bill Total Amount ($)") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldLime, unfocusedBorderColor = DarkBorder)
    )

    Text("Tip Percentage", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        listOf(10, 15, 18, 20, 25).forEach { pct ->
            FilterChip(
                selected = tipPercent == pct,
                onClick = { tipPercent = pct },
                label = { Text("$pct%") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = EmeraldLime,
                    selectedLabelColor = DarkBg,
                    containerColor = Color(0x331E293B),
                    labelColor = TextPrimary
                )
            )
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Split Among People", fontSize = 14.sp, color = TextPrimary)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { if (peopleCount > 1) peopleCount-- },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0x331E293B))
            ) { Text("-", fontSize = 18.sp, color = TextPrimary) }
            Text("$peopleCount", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = EmeraldLime)
            Button(
                onClick = { peopleCount++ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0x331E293B))
            ) { Text("+", fontSize = 18.sp, color = TextPrimary) }
        }
    }

    val bill = billText.toDoubleOrNull() ?: 0.0
    val tipAmount = bill * (tipPercent / 100.0)
    val totalAmount = bill + tipAmount
    val perPerson = totalAmount / peopleCount.coerceAtLeast(1)

    FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Tip Amount ($tipPercent%):", color = TextMuted)
                Text(String.format(Locale.US, "$%.2f", tipAmount), fontWeight = FontWeight.Bold, color = SunsetGold)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Grand Total:", color = TextMuted)
                Text(String.format(Locale.US, "$%.2f", totalAmount), fontWeight = FontWeight.Bold, color = TextPrimary)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Each Person Pays:", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ElectricCyan)
                Text(
                    String.format(Locale.US, "$%.2f", perPerson),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = EmeraldLime
                )
            }
        }
    }
}

@Composable
fun DiscountTaxModalView() {
    var priceText by remember { mutableStateOf("120") }
    var discountText by remember { mutableStateOf("20") }
    var taxText by remember { mutableStateOf("8.5") }

    OutlinedTextField(
        value = priceText,
        onValueChange = { priceText = it },
        label = { Text("Original Price ($)") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldLime, unfocusedBorderColor = DarkBorder)
    )

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = discountText,
            onValueChange = { discountText = it },
            label = { Text("Discount (%)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldLime, unfocusedBorderColor = DarkBorder)
        )
        OutlinedTextField(
            value = taxText,
            onValueChange = { taxText = it },
            label = { Text("Sales Tax (%)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldLime, unfocusedBorderColor = DarkBorder)
        )
    }

    val price = priceText.toDoubleOrNull() ?: 0.0
    val disc = discountText.toDoubleOrNull() ?: 0.0
    val tax = taxText.toDoubleOrNull() ?: 0.0

    val savedAmount = price * (disc / 100.0)
    val afterDiscount = price - savedAmount
    val taxAmount = afterDiscount * (tax / 100.0)
    val finalPrice = afterDiscount + taxAmount

    FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total Savings:", color = EmeraldLime, fontWeight = FontWeight.SemiBold)
                Text(String.format(Locale.US, "-$%.2f", savedAmount), fontWeight = FontWeight.Bold, color = EmeraldLime)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Tax Added:", color = TextMuted)
                Text(String.format(Locale.US, "+$%.2f", taxAmount), fontWeight = FontWeight.Bold, color = SunsetGold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Final Total:", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(
                    String.format(Locale.US, "$%.2f", finalPrice),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ElectricCyan
                )
            }
        }
    }
}

@Composable
fun BmiCalculatorModalView() {
    var weightText by remember { mutableStateOf("72") }
    var heightText by remember { mutableStateOf("178") }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = weightText,
            onValueChange = { weightText = it },
            label = { Text("Weight (kg)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldLime, unfocusedBorderColor = DarkBorder)
        )
        OutlinedTextField(
            value = heightText,
            onValueChange = { heightText = it },
            label = { Text("Height (cm)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldLime, unfocusedBorderColor = DarkBorder)
        )
    }

    val weight = weightText.toDoubleOrNull() ?: 0.0
    val heightM = (heightText.toDoubleOrNull() ?: 0.0) / 100.0

    val bmi = if (heightM > 0) weight / (heightM * heightM) else 0.0
    val (category, catColor) = when {
        bmi < 18.5 -> "Underweight" to SunsetGold
        bmi < 24.9 -> "Normal Healthy Weight ✓" to EmeraldLime
        bmi < 29.9 -> "Overweight" to SunsetGold
        else -> "Obese" to NeonRose
    }

    FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Body Mass Index (BMI)", fontSize = 13.sp, color = TextMuted)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = String.format(Locale.US, "%.1f", bmi),
                fontSize = 38.sp,
                fontWeight = FontWeight.ExtraBold,
                color = catColor
            )
            Text(
                text = category,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = catColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            val minHealthy = 18.5 * heightM * heightM
            val maxHealthy = 24.9 * heightM * heightM
            if (heightM > 0) {
                Text(
                    text = String.format(Locale.US, "Healthy Weight Range: %.1f - %.1f kg", minHealthy, maxHealthy),
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun AgeCalculatorModalView() {
    var birthYearText by remember { mutableStateOf("2000") }
    var birthMonthText by remember { mutableStateOf("5") }
    var birthDayText by remember { mutableStateOf("15") }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = birthYearText,
            onValueChange = { birthYearText = it },
            label = { Text("Year") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1.2f),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldLime, unfocusedBorderColor = DarkBorder)
        )
        OutlinedTextField(
            value = birthMonthText,
            onValueChange = { birthMonthText = it },
            label = { Text("Month") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldLime, unfocusedBorderColor = DarkBorder)
        )
        OutlinedTextField(
            value = birthDayText,
            onValueChange = { birthDayText = it },
            label = { Text("Day") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldLime, unfocusedBorderColor = DarkBorder)
        )
    }

    val y = birthYearText.toIntOrNull() ?: 2000
    val m = (birthMonthText.toIntOrNull() ?: 1).coerceIn(1, 12)
    val d = (birthDayText.toIntOrNull() ?: 1).coerceIn(1, 31)

    val currentYear = 2026
    val currentMonth = 10
    val currentDay = 31

    var years = currentYear - y
    var months = currentMonth - m
    var days = currentDay - d

    if (days < 0) {
        months -= 1
        days += 30
    }
    if (months < 0) {
        years -= 1
        months += 12
    }

    val totalDaysApprox = (years * 365) + (months * 30) + days

    FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Exact Age", fontSize = 13.sp, color = TextMuted)
            Text(
                text = "$years Years, $months Months, $days Days",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = ElectricCyan
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total Days Lived:", color = TextMuted, fontSize = 13.sp)
                Text("~$totalDaysApprox days", fontWeight = FontWeight.Bold, color = EmeraldLime)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total Hours Lived:", color = TextMuted, fontSize = 13.sp)
                Text("~${totalDaysApprox * 24} hours", fontWeight = FontWeight.Bold, color = SunsetGold)
            }
        }
    }
}

@Composable
fun PercentageCalculatorModalView() {
    var mode by remember { mutableIntStateOf(0) }
    var valA by remember { mutableStateOf("25") }
    var valB by remember { mutableStateOf("200") }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        listOf("X% of Y", "X is ?% of Y", "% Change").forEachIndexed { index, label ->
            FilterChip(
                selected = mode == index,
                onClick = { mode = index },
                label = { Text(label, fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = EmeraldLime,
                    selectedLabelColor = DarkBg,
                    containerColor = Color(0x331E293B),
                    labelColor = TextPrimary
                )
            )
        }
    }

    val a = valA.toDoubleOrNull() ?: 0.0
    val b = valB.toDoubleOrNull() ?: 1.0

    when (mode) {
        0 -> {
            OutlinedTextField(
                value = valA,
                onValueChange = { valA = it },
                label = { Text("Percentage (%)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldLime, unfocusedBorderColor = DarkBorder)
            )
            OutlinedTextField(
                value = valB,
                onValueChange = { valB = it },
                label = { Text("Of Number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldLime, unfocusedBorderColor = DarkBorder)
            )
            val res = b * (a / 100.0)
            FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$a% of $b is", color = TextMuted, fontSize = 13.sp)
                    Text(String.format(Locale.US, "%.2f", res), fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = EmeraldLime)
                }
            }
        }
        1 -> {
            OutlinedTextField(
                value = valA,
                onValueChange = { valA = it },
                label = { Text("Part Value") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldLime, unfocusedBorderColor = DarkBorder)
            )
            OutlinedTextField(
                value = valB,
                onValueChange = { valB = it },
                label = { Text("Total Value") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldLime, unfocusedBorderColor = DarkBorder)
            )
            val res = if (b != 0.0) (a / b) * 100.0 else 0.0
            FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$a is what % of $b", color = TextMuted, fontSize = 13.sp)
                    Text(String.format(Locale.US, "%.2f%%", res), fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = ElectricCyan)
                }
            }
        }
        2 -> {
            OutlinedTextField(
                value = valA,
                onValueChange = { valA = it },
                label = { Text("Original Value (From)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldLime, unfocusedBorderColor = DarkBorder)
            )
            OutlinedTextField(
                value = valB,
                onValueChange = { valB = it },
                label = { Text("New Value (To)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldLime, unfocusedBorderColor = DarkBorder)
            )
            val diff = b - a
            val pctChange = if (a != 0.0) (diff / a) * 100.0 else 0.0
            val color = if (diff >= 0) EmeraldLime else NeonRose
            val prefix = if (diff >= 0) "+" else ""
            FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Change from $a to $b", color = TextMuted, fontSize = 13.sp)
                    Text("$prefix${String.format(Locale.US, "%.2f%%", pctChange)}", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = color)
                }
            }
        }
    }
}
