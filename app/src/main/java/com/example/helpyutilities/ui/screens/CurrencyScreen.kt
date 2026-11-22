package com.example.helpyutilities.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import com.example.helpyutilities.data.AppPreferences
import com.example.helpyutilities.widget.HelpyHomeWidgetBridge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.helpyutilities.ui.components.FrostedGlassCard
import com.example.helpyutilities.ui.components.rememberAdaptiveMetrics
import com.example.helpyutilities.ui.theme.ElectricCyan
import com.example.helpyutilities.ui.theme.EmeraldLime
import com.example.helpyutilities.ui.theme.LocalAppTheme
import com.example.helpyutilities.ui.theme.SunsetGold
import com.example.helpyutilities.util.HapticFeedbackHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

data class CurrencyItem(val code: String, val name: String, val flag: String, val rateToUsd: Double)

// Complete world currencies (160+ ISO 4217 fiat currencies + top global cryptocurrencies)
val fullWorldCurrenciesList = listOf(
    // Major Global
    CurrencyItem("USD", "United States Dollar", "🇺🇸", 1.0),
    CurrencyItem("EUR", "Euro (European Union)", "🇪🇺", 0.92),
    CurrencyItem("GBP", "British Pound Sterling", "🇬🇧", 0.79),
    CurrencyItem("JPY", "Japanese Yen", "🇯🇵", 155.2),
    CurrencyItem("CAD", "Canadian Dollar", "🇨🇦", 1.35),
    CurrencyItem("AUD", "Australian Dollar", "🇦🇺", 1.52),
    CurrencyItem("CHF", "Swiss Franc", "🇨🇭", 0.89),
    CurrencyItem("CNY", "Chinese Yuan Renminbi", "🇨🇳", 7.23),
    CurrencyItem("INR", "Indian Rupee", "🇮🇳", 83.3),
    CurrencyItem("BRL", "Brazilian Real", "🇧🇷", 5.10),
    CurrencyItem("ZAR", "South African Rand", "🇿🇦", 18.5),
    CurrencyItem("MXN", "Mexican Peso", "🇲🇽", 16.8),
    CurrencyItem("SGD", "Singapore Dollar", "🇸🇬", 1.35),
    CurrencyItem("NZD", "New Zealand Dollar", "🇳🇿", 1.63),
    CurrencyItem("HKD", "Hong Kong Dollar", "🇭🇰", 7.82),
    CurrencyItem("SEK", "Swedish Krona", "🇸🇪", 10.8),
    CurrencyItem("NOK", "Norwegian Krone", "🇳🇴", 10.6),
    CurrencyItem("KRW", "South Korean Won", "🇰🇷", 1375.0),
    CurrencyItem("TRY", "Turkish Lira", "🇹🇷", 32.2),
    CurrencyItem("RUB", "Russian Ruble", "🇷🇺", 92.5),
    CurrencyItem("AED", "United Arab Emirates Dirham", "🇦🇪", 3.67),
    CurrencyItem("SAR", "Saudi Arabian Riyal", "🇸🇦", 3.75),
    CurrencyItem("THB", "Thai Baht", "🇹🇭", 36.8),
    CurrencyItem("MYR", "Malaysian Ringgit", "🇲🇾", 4.75),
    CurrencyItem("IDR", "Indonesian Rupiah", "🇮🇩", 16200.0),
    CurrencyItem("PHP", "Philippine Peso", "🇵🇭", 57.5),
    CurrencyItem("EGP", "Egyptian Pound", "🇪🇬", 47.8),
    CurrencyItem("NGN", "Nigerian Naira", "🇳🇬", 1350.0),
    CurrencyItem("KES", "Kenyan Shilling", "🇰🇪", 132.0),
    CurrencyItem("DZD", "Algerian Dinar", "🇩🇿", 134.5),
    CurrencyItem("MAD", "Moroccan Dirham", "🇲🇦", 10.1),
    CurrencyItem("PKR", "Pakistani Rupee", "🇵🇰", 278.0),
    CurrencyItem("BDT", "Bangladeshi Taka", "🇧🇩", 117.0),
    CurrencyItem("VND", "Vietnamese Dong", "🇻🇳", 25400.0),
    CurrencyItem("TWD", "New Taiwan Dollar", "🇹🇼", 32.4),
    CurrencyItem("ARS", "Argentine Peso", "🇦🇷", 880.0),
    CurrencyItem("CLP", "Chilean Peso", "🇨🇱", 940.0),
    CurrencyItem("COP", "Colombian Peso", "🇨🇴", 3890.0),
    CurrencyItem("PEN", "Peruvian Sol", "🇵🇪", 3.72),
    CurrencyItem("ILS", "Israeli New Shekel", "🇮🇱", 3.70),
    CurrencyItem("PLN", "Polish Zloty", "🇵🇱", 4.02),
    CurrencyItem("CZK", "Czech Koruna", "🇨🇿", 23.1),
    CurrencyItem("HUF", "Hungarian Forint", "🇭🇺", 362.0),
    CurrencyItem("RON", "Romanian Leu", "🇷🇴", 4.58),
    CurrencyItem("BGN", "Bulgarian Lev", "🇧🇬", 1.80),
    CurrencyItem("DKK", "Danish Krone", "🇩🇰", 6.88),
    CurrencyItem("ISK", "Icelandic Krona", "🇮🇸", 138.5),
    CurrencyItem("KWD", "Kuwaiti Dinar", "🇰🇼", 0.31),
    CurrencyItem("BHD", "Bahraini Dinar", "🇧🇭", 0.38),
    CurrencyItem("OMR", "Omani Rial", "🇴🇲", 0.385),
    CurrencyItem("QAR", "Qatari Riyal", "🇶🇦", 3.64),
    CurrencyItem("JOD", "Jordanian Dinar", "🇯🇴", 0.71),
    CurrencyItem("IQD", "Iraqi Dinar", "🇮🇶", 1310.0),
    CurrencyItem("KZT", "Kazakhstani Tenge", "🇰🇿", 445.0),
    CurrencyItem("UAH", "Ukrainian Hryvnia", "🇺🇦", 39.5),
    CurrencyItem("GEL", "Georgian Lari", "🇬🇪", 2.68),
    CurrencyItem("AMD", "Armenian Dram", "🇦🇲", 388.0),
    CurrencyItem("AZN", "Azerbaijani Manat", "🇦🇿", 1.70),
    CurrencyItem("UZS", "Uzbekistani Som", "🇺🇿", 12650.0),
    CurrencyItem("GHS", "Ghanaian Cedi", "🇬🇭", 14.5),
    CurrencyItem("TZS", "Tanzanian Shilling", "🇹🇿", 2580.0),
    CurrencyItem("UGX", "Ugandan Shilling", "🇺🇬", 3800.0),
    CurrencyItem("ETB", "Ethiopian Birr", "🇪🇹", 57.0),
    CurrencyItem("RWF", "Rwandan Franc", "🇷🇼", 1290.0),
    CurrencyItem("XOF", "West African CFA Franc", "🌍", 605.0),
    CurrencyItem("XAF", "Central African CFA Franc", "🌍", 605.0),
    CurrencyItem("CRC", "Costa Rican Colon", "🇨🇷", 515.0),
    CurrencyItem("DOP", "Dominican Peso", "🇩🇴", 58.5),
    CurrencyItem("GTQ", "Guatemalan Quetzal", "🇬🇹", 7.78),
    CurrencyItem("HNL", "Honduran Lempira", "🇭🇳", 24.7),
    CurrencyItem("NIO", "Nicaraguan Cordoba", "🇳🇮", 36.8),
    CurrencyItem("PAB", "Panamanian Balboa", "🇵🇦", 1.0),
    CurrencyItem("PYG", "Paraguayan Guarani", "🇵🇾", 7450.0),
    CurrencyItem("UYU", "Uruguayan Peso", "🇺🇾", 38.6),
    CurrencyItem("BOB", "Bolivian Boliviano", "🇧🇴", 6.91),
    CurrencyItem("AFN", "Afghan Afghani", "🇦🇫", 71.0),
    CurrencyItem("ALL", "Albanian Lek", "🇦🇱", 93.5),
    CurrencyItem("ANG", "Netherlands Antillean Guilder", "🇨🇼", 1.80),
    CurrencyItem("AOA", "Angolan Kwanza", "🇦🇴", 840.0),
    CurrencyItem("AWG", "Aruban Florin", "🇦🇼", 1.80),
    CurrencyItem("BAM", "Bosnia-Herzegovina Mark", "🇧🇦", 1.80),
    CurrencyItem("BBD", "Barbadian Dollar", "🇧🇧", 2.0),
    CurrencyItem("BIF", "Burundian Franc", "🇧🇮", 2860.0),
    CurrencyItem("BMD", "Bermudian Dollar", "🇧🇲", 1.0),
    CurrencyItem("BND", "Brunei Dollar", "🇧🇳", 1.35),
    CurrencyItem("BSD", "Bahamian Dollar", "🇧🇸", 1.0),
    CurrencyItem("BTN", "Bhutanese Ngultrum", "🇧🇹", 83.3),
    CurrencyItem("BWP", "Botswanan Pula", "🇧🇼", 13.6),
    CurrencyItem("BYN", "Belarusian Ruble", "🇧🇾", 3.27),
    CurrencyItem("BZD", "Belize Dollar", "🇧🇿", 2.0),
    CurrencyItem("CDF", "Congolese Franc", "🇨🇩", 2780.0),
    CurrencyItem("CVE", "Cape Verdean Escudo", "🇨🇻", 102.0),
    CurrencyItem("DJF", "Djiboutian Franc", "🇩🇯", 178.0),
    CurrencyItem("ERN", "Eritrean Nakfa", "🇪🇷", 15.0),
    CurrencyItem("FJD", "Fijian Dollar", "🇫🇯", 2.25),
    CurrencyItem("FKP", "Falkland Islands Pound", "🇫🇰", 0.79),
    CurrencyItem("GIP", "Gibraltar Pound", "🇬🇮", 0.79),
    CurrencyItem("GMD", "Gambian Dalasi", "🇬🇲", 68.0),
    CurrencyItem("GNF", "Guinean Franc", "🇬🇳", 8600.0),
    CurrencyItem("GYD", "Guyanaese Dollar", "🇬🇾", 209.0),
    CurrencyItem("HTG", "Haitian Gourde", "🇭🇹", 132.0),
    CurrencyItem("JMD", "Jamaican Dollar", "🇯🇲", 156.0),
    CurrencyItem("KGS", "Kyrgystani Som", "🇰🇬", 88.0),
    CurrencyItem("KHR", "Cambodian Riel", "🇰🇭", 4080.0),
    CurrencyItem("KMF", "Comorian Franc", "🇰🇲", 453.0),
    CurrencyItem("KYD", "Cayman Islands Dollar", "🇰🇾", 0.83),
    CurrencyItem("LAK", "Laotian Kip", "🇱🇦", 21500.0),
    CurrencyItem("LKR", "Sri Lankan Rupee", "🇱🇰", 302.0),
    CurrencyItem("LRD", "Liberian Dollar", "🇱🇷", 192.0),
    CurrencyItem("LSL", "Lesotho Loti", "🇱🇸", 18.5),
    CurrencyItem("LYD", "Libyan Dinar", "🇱🇾", 4.85),
    CurrencyItem("MDL", "Moldovan Leu", "🇲🇩", 17.7),
    CurrencyItem("MGA", "Malagasy Ariary", "🇲🇬", 4450.0),
    CurrencyItem("MKD", "Macedonian Denar", "🇲🇰", 56.7),
    CurrencyItem("MMK", "Myanmar Kyat", "🇲🇲", 2100.0),
    CurrencyItem("MNT", "Mongolian Tugrik", "🇲🇳", 3380.0),
    CurrencyItem("MOP", "Macanese Pataca", "🇲🇴", 8.05),
    CurrencyItem("MRU", "Mauritanian Ouguiya", "🇲🇷", 39.6),
    CurrencyItem("MUR", "Mauritian Rupee", "🇲🇺", 46.2),
    CurrencyItem("MVR", "Maldivian Rufiyaa", "🇲🇻", 15.4),
    CurrencyItem("MWK", "Malawian Kwacha", "🇲🇼", 1730.0),
    CurrencyItem("MZN", "Mozambican Metical", "🇲🇿", 63.8),
    CurrencyItem("NAD", "Namibian Dollar", "🇳🇦", 18.5),
    CurrencyItem("NPR", "Nepalese Rupee", "🇳🇵", 133.0),
    CurrencyItem("PGK", "Papua New Guinean Kina", "🇵🇬", 3.85),
    CurrencyItem("RSD", "Serbian Dinar", "🇷🇸", 108.0),
    CurrencyItem("SBD", "Solomon Islands Dollar", "🇸🇧", 8.45),
    CurrencyItem("SCR", "Seychellois Rupee", "🇸🇨", 13.5),
    CurrencyItem("SDG", "Sudanese Pound", "🇸🇩", 590.0),
    CurrencyItem("SOS", "Somali Shilling", "🇸🇴", 570.0),
    CurrencyItem("SRD", "Surinamese Dollar", "🇸🇷", 32.5),
    CurrencyItem("SSP", "South Sudanese Pound", "🇸🇸", 1500.0),
    CurrencyItem("STN", "Sao Tome & Principe Dobra", "🇸🇹", 22.6),
    CurrencyItem("SYP", "Syrian Pound", "🇸🇾", 13000.0),
    CurrencyItem("SZL", "Swazi Lilangeni", "🇸🇿", 18.5),
    CurrencyItem("TJS", "Tajikistani Somoni", "🇹🇯", 10.9),
    CurrencyItem("TMT", "Turkmenistani Manat", "🇹🇲", 3.50),
    CurrencyItem("TND", "Tunisian Dinar", "🇹🇳", 3.12),
    CurrencyItem("TOP", "Tongan Paʻanga", "🇹🇴", 2.36),
    CurrencyItem("TTD", "Trinidad and Tobago Dollar", "🇹🇹", 6.78),
    CurrencyItem("TVD", "Tuvaluan Dollar", "🇹🇻", 1.52),
    CurrencyItem("VES", "Venezuelan Bolivar", "🇻🇪", 36.5),
    CurrencyItem("VUV", "Vanuatu Vatu", "🇻🇺", 119.0),
    CurrencyItem("WST", "Samoan Tala", "🇼🇸", 2.75),
    CurrencyItem("XCD", "East Caribbean Dollar", "🇦🇬", 2.70),
    CurrencyItem("YER", "Yemeni Rial", "🇾🇪", 250.0),
    CurrencyItem("ZMW", "Zambian Kwacha", "🇿🇲", 26.5),
    CurrencyItem("ZWL", "Zimbabwean Dollar", "🇿🇼", 13.5),
    // Cryptocurrencies
    CurrencyItem("BTC", "Bitcoin", "₿", 0.000015),
    CurrencyItem("ETH", "Ethereum", "Ξ", 0.00031),
    CurrencyItem("SOL", "Solana", "◎", 0.0068),
    CurrencyItem("XRP", "Ripple XRP", "✕", 1.82),
    CurrencyItem("DOGE", "Dogecoin", "Ð", 6.6),
    CurrencyItem("ADA", "Cardano", "₳", 2.1),
    CurrencyItem("USDT", "Tether USD", "₮", 1.0),
    CurrencyItem("USDC", "USD Coin", "💵", 1.0)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentTheme = LocalAppTheme.current
    val colorScheme = MaterialTheme.colorScheme
    val adaptive = rememberAdaptiveMetrics()

    val savedFromCode by AppPreferences.workspaceCurrencyFrom.collectAsState()
    val savedToCode by AppPreferences.workspaceCurrencyTo.collectAsState()

    var amountText by remember { mutableStateOf("100") }
    var fromCurr by remember(savedFromCode) {
        mutableStateOf(fullWorldCurrenciesList.firstOrNull { it.code == savedFromCode } ?: fullWorldCurrenciesList[0])
    }
    var toCurr by remember(savedToCode) {
        mutableStateOf(fullWorldCurrenciesList.firstOrNull { it.code == savedToCode } ?: fullWorldCurrenciesList[1])
    }

    val liveCurrencies = remember { mutableStateListOf(*fullWorldCurrenciesList.toTypedArray()) }
    var isRefreshing by remember { mutableStateOf(false) }
    var lastUpdatedText by remember { mutableStateOf("Live Online Rates") }

    var pickingForFrom by remember { mutableStateOf(true) }
    var showCurrencySheet by remember { mutableStateOf(false) }

    val searchQueryState = remember { MutableStateFlow("") }
    val searchQuery by searchQueryState.collectAsState()

    val filteredCurrencies = remember(searchQuery, liveCurrencies.size) {
        if (searchQuery.isBlank()) {
            liveCurrencies
        } else {
            liveCurrencies.filter {
                it.code.contains(searchQuery, ignoreCase = true) || it.name.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    suspend fun fetchRates() {
        isRefreshing = true
        withContext(Dispatchers.IO) {
            var fetched = false
            // Endpoint 1: Open Exchange Rates free API (160+ world fiat and top cryptos)
            try {
                val url = URL("https://open.er-api.com/v6/latest/USD")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.connectTimeout = 5000
                conn.readTimeout = 5000

                if (conn.responseCode == 200) {
                    val body = conn.inputStream.bufferedReader().readText()
                    val json = JSONObject(body)
                    if (json.has("rates")) {
                        val ratesObj = json.getJSONObject("rates")
                        val updatedList = fullWorldCurrenciesList.map { item ->
                            if (ratesObj.has(item.code)) {
                                item.copy(rateToUsd = ratesObj.getDouble(item.code))
                            } else item
                        }
                        withContext(Dispatchers.Main) {
                            liveCurrencies.clear()
                            liveCurrencies.addAll(updatedList)
                            fromCurr = liveCurrencies.firstOrNull { it.code == fromCurr.code } ?: fromCurr
                            toCurr = liveCurrencies.firstOrNull { it.code == toCurr.code } ?: toCurr
                            lastUpdatedText = "160+ Live Forex Rates Updated ✓"
                        }
                        fetched = true
                    }
                }
            } catch (_: Exception) {}

            // Endpoint 2: Fallback to Fawazahmed0 Currency API if Endpoint 1 fails
            if (!fetched) {
                try {
                    val fallbackUrl = URL("https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies/usd.json")
                    val conn = fallbackUrl.openConnection() as HttpURLConnection
                    conn.requestMethod = "GET"
                    conn.connectTimeout = 5000
                    conn.readTimeout = 5000

                    if (conn.responseCode == 200) {
                        val body = conn.inputStream.bufferedReader().readText()
                        val json = JSONObject(body)
                        if (json.has("usd")) {
                            val ratesObj = json.getJSONObject("usd")
                            val updatedList = fullWorldCurrenciesList.map { item ->
                                val codeLower = item.code.lowercase()
                                if (ratesObj.has(codeLower)) {
                                    item.copy(rateToUsd = ratesObj.getDouble(codeLower))
                                } else item
                            }
                            withContext(Dispatchers.Main) {
                                liveCurrencies.clear()
                                liveCurrencies.addAll(updatedList)
                                fromCurr = liveCurrencies.firstOrNull { it.code == fromCurr.code } ?: fromCurr
                                toCurr = liveCurrencies.firstOrNull { it.code == toCurr.code } ?: toCurr
                                lastUpdatedText = "160+ Live Rates Updated (Mirror) ✓"
                            }
                            fetched = true
                        }
                    }
                } catch (_: Exception) {}
            }

            if (!fetched) {
                withContext(Dispatchers.Main) {
                    lastUpdatedText = "Offline Cached Rates (100% Usable)"
                }
            }
            withContext(Dispatchers.Main) {
                isRefreshing = false
                val zarItem = liveCurrencies.firstOrNull { it.code == "ZAR" }
                val zarRate = zarItem?.rateToUsd ?: 18.42
                val widgetRateStr = "1 USD = ${String.format(Locale.US, "%.2f", zarRate)} ZAR"
                HelpyHomeWidgetBridge.syncCurrency(context, widgetRateStr, "Live Rates Updated")
            }
        }
    }

    LaunchedEffect(Unit) {
        fetchRates()
    }

    val amountDouble = amountText.toDoubleOrNull() ?: 0.0
    val convertedValue = if (fromCurr.rateToUsd > 0) (amountDouble / fromCurr.rateToUsd) * toCurr.rateToUsd else 0.0
    val unitRate = if (fromCurr.rateToUsd > 0) (1.0 / fromCurr.rateToUsd) * toCurr.rateToUsd else 0.0

    val quickAmounts = listOf("10", "50", "100", "500", "1,000", "5,000")

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp)
                .padding(horizontal = adaptive.horizontalScreenPadding, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CurrencyExchange,
                            contentDescription = null,
                            tint = currentTheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Global Currency Exchange",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurface
                            )
                            Text(
                                text = lastUpdatedText,
                                fontSize = 12.sp,
                                color = if (isRefreshing) SunsetGold else EmeraldLime
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            HapticFeedbackHelper.trigger(context)
                            coroutineScope.launch { fetchRates() }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Rates",
                            tint = if (isRefreshing) SunsetGold else currentTheme.primary
                        )
                    }
                }
            }

            // Primary Conversion Card
            item {
                FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Currency Selector Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = {
                                    HapticFeedbackHelper.trigger(context)
                                    pickingForFrom = true
                                    showCurrencySheet = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = currentTheme.surfaceContainerHigh),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.weight(1f).defaultMinSize(minWidth = 84.dp).height(48.dp)
                            ) {
                                Text(
                                    text = "${fromCurr.flag} ${fromCurr.code}",
                                    color = currentTheme.primary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }

                            IconButton(
                                onClick = {
                                    HapticFeedbackHelper.trigger(context)
                                    val tmp = fromCurr
                                    fromCurr = toCurr
                                    toCurr = tmp
                                    AppPreferences.setWorkspaceCurrencyPair(fromCurr.code, toCurr.code)
                                },
                                modifier = Modifier.padding(horizontal = 6.dp).size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SwapHoriz,
                                    contentDescription = "Swap Currencies",
                                    tint = currentTheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Button(
                                onClick = {
                                    HapticFeedbackHelper.trigger(context)
                                    pickingForFrom = false
                                    showCurrencySheet = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = currentTheme.surfaceContainerHigh),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.weight(1f).defaultMinSize(minWidth = 84.dp).height(48.dp)
                            ) {
                                Text(
                                    text = "${toCurr.flag} ${toCurr.code}",
                                    color = currentTheme.secondary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Input Field
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { amountText = it.filter { char -> char.isDigit() || char == '.' } },
                            label = { Text("Amount in ${fromCurr.code}") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = currentTheme.primary,
                                unfocusedBorderColor = currentTheme.borderColor,
                                focusedTextColor = colorScheme.onSurface,
                                unfocusedTextColor = colorScheme.onSurface
                            )
                        )

                        // Quick Amount Chips
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(quickAmounts) { amt ->
                                FilterChip(
                                    selected = amountText == amt.replace(",", ""),
                                    onClick = {
                                        HapticFeedbackHelper.trigger(context)
                                        amountText = amt.replace(",", "")
                                    },
                                    label = { Text("${fromCurr.flag} $amt", fontSize = 12.sp, maxLines = 1) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = currentTheme.primary,
                                        selectedLabelColor = currentTheme.onPrimary,
                                        containerColor = currentTheme.surfaceContainerHigh,
                                        labelColor = colorScheme.onSurface
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }

                        // Converted Output Display
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(currentTheme.surfaceContainerHigh)
                                .border(1.dp, currentTheme.borderColor, RoundedCornerShape(16.dp))
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Converted Result", fontSize = 12.sp, color = colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = String.format(Locale.US, "%,.2f %s", convertedValue, toCurr.code),
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (currentTheme.isLight) currentTheme.primary else EmeraldLime,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "1 ${fromCurr.code} = ${String.format(Locale.US, "%.4f", unitRate)} ${toCurr.code}",
                                    fontSize = 12.sp,
                                    color = colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Section Title: Multi-Currency Live Board
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "160+ World Currencies Board",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                    Text(
                        text = "${amountDouble.toInt()} ${fromCurr.code} Equals",
                        fontSize = 12.sp,
                        color = currentTheme.primary
                    )
                }
            }

            // Multi-Currency Live Conversion Items
            items(liveCurrencies.filter { it.code != fromCurr.code }) { item ->
                val converted = if (fromCurr.rateToUsd > 0) (amountDouble / fromCurr.rateToUsd) * item.rateToUsd else 0.0
                FrostedGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            HapticFeedbackHelper.trigger(context)
                            toCurr = item
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(item.flag, fontSize = 24.sp)
                            Column {
                                Text(
                                    text = item.code,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.onSurface
                                )
                                Text(
                                    text = item.name,
                                    fontSize = 12.sp,
                                    color = colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = String.format(Locale.US, "%,.2f", converted),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = if (item.code == toCurr.code) (if (currentTheme.isLight) currentTheme.primary else EmeraldLime) else colorScheme.onSurface
                            )
                            Text(
                                text = "1 ${fromCurr.code} = ${String.format(Locale.US, "%.3f", if (fromCurr.rateToUsd > 0) item.rateToUsd / fromCurr.rateToUsd else 0.0)}",
                                fontSize = 11.sp,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(96.dp))
            }
        }
    }

    // Currency Picker Bottom Sheet
    if (showCurrencySheet) {
        ModalBottomSheet(
            onDismissRequest = { showCurrencySheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = currentTheme.cardBackground,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Text(
                    text = if (pickingForFrom) "Select 'From' Currency" else "Select 'To' Currency",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQueryState.value = it },
                    placeholder = { Text("Search 160+ world currencies...", color = colorScheme.onSurfaceVariant) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = currentTheme.primary) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQueryState.value = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = colorScheme.onSurfaceVariant)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = currentTheme.primary,
                        unfocusedBorderColor = currentTheme.borderColor,
                        focusedTextColor = colorScheme.onSurface,
                        unfocusedTextColor = colorScheme.onSurface
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(filteredCurrencies) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    HapticFeedbackHelper.trigger(context)
                                    if (pickingForFrom) {
                                        fromCurr = item
                                        AppPreferences.setWorkspaceCurrencyPair(item.code, toCurr.code)
                                    } else {
                                        toCurr = item
                                        AppPreferences.setWorkspaceCurrencyPair(fromCurr.code, item.code)
                                    }
                                    showCurrencySheet = false
                                }
                                .padding(vertical = 12.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(item.flag, fontSize = 24.sp)
                                Column {
                                    Text(item.code, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
                                    Text(item.name, fontSize = 12.sp, color = colorScheme.onSurfaceVariant)
                                }
                            }
                            Text(
                                text = "1 USD = ${String.format(Locale.US, "%.2f", item.rateToUsd)}",
                                fontSize = 12.sp,
                                color = currentTheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
