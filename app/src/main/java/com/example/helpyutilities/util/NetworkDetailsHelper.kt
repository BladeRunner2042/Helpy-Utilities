package com.example.helpyutilities.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.os.Build
import java.net.Inet4Address

data class LocalNetworkDetails(
    val transportType: String,
    val localIpv4: String,
    val gatewayIp: String,
    val dnsServers: String,
    val wifiBand: String,
    val linkSpeedMbps: Int
)

object NetworkDetailsHelper {
    fun getDetails(context: Context): LocalNetworkDetails {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetwork
        val caps = cm.getNetworkCapabilities(activeNetwork)
        val linkProps = cm.getLinkProperties(activeNetwork)

        val transport = when {
            caps == null -> "Offline"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Mobile Data"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            else -> "Connected"
        }

        val ipv4 = linkProps?.linkAddresses?.map { it.address }
            ?.firstOrNull { it is Inet4Address && !it.isLoopbackAddress }?.hostAddress ?: "Unavailable"
        val gateway = linkProps?.routes?.firstOrNull { it.isDefaultRoute && it.gateway is Inet4Address }
            ?.gateway?.hostAddress ?: "Unavailable"
        val dns = linkProps?.dnsServers?.mapNotNull { it.hostAddress }?.joinToString(", ")
            ?.ifEmpty { "Automatic" } ?: "Unavailable"

        var band = "N/A"
        var linkSpeed = -1
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val wifiInfo = caps?.transportInfo as? WifiInfo
            if (wifiInfo != null) {
                linkSpeed = wifiInfo.linkSpeed
                val freq = wifiInfo.frequency
                band = when {
                    freq in 2400..2500 -> "2.4 GHz ($freq MHz)"
                    freq in 4900..5900 -> "5 GHz ($freq MHz)"
                    freq in 5925..7125 -> "6 GHz ($freq MHz)"
                    else -> "$freq MHz"
                }
            }
        }

        return LocalNetworkDetails(transport, ipv4, gateway, dns, band, linkSpeed)
    }
}
