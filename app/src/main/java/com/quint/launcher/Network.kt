package com.quint.launcher

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

enum class NetType { WIFI, ETHERNET, OTHER, NONE }

private fun currentNetType(cm: ConnectivityManager): NetType {
    val network = cm.activeNetwork ?: return NetType.NONE
    val caps = cm.getNetworkCapabilities(network) ?: return NetType.NONE
    return when {
        caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetType.WIFI
        caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetType.ETHERNET
        caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) -> NetType.OTHER
        else -> NetType.NONE
    }
}

@Composable
fun rememberNetType(): NetType {
    val context = LocalContext.current
    val cm = remember { context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager }
    var type by remember { mutableStateOf(currentNetType(cm)) }

    DisposableEffect(cm) {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                type = currentNetType(cm)
            }

            override fun onLost(network: Network) {
                type = currentNetType(cm)
            }

            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                type = currentNetType(cm)
            }
        }
        runCatching { cm.registerDefaultNetworkCallback(callback) }
        onDispose { runCatching { cm.unregisterNetworkCallback(callback) } }
    }
    return type
}
