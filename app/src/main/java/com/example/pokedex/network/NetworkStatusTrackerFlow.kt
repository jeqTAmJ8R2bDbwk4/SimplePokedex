package com.example.pokedex.network

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.example.pokedex.models.NetworkStatus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow


private fun getNetworkStatus(connectivityManager: ConnectivityManager): NetworkStatus {
    val network = connectivityManager.activeNetwork ?: return NetworkStatus.NoNetwork
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return NetworkStatus.NoNetwork

    return when {
        !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) -> NetworkStatus.NoInternetCapability
        !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) -> NetworkStatus.NoValidatedInternet
        else -> NetworkStatus.Connected
    }
}


fun networkStatusTrackerFlow(connectivityManager: ConnectivityManager) = callbackFlow {
    val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            trySend(getNetworkStatus(connectivityManager))
        }

        override fun onLost(network: Network) {
            trySend(NetworkStatus.NoNetwork)
        }

        override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
            trySend(getNetworkStatus(connectivityManager))
        }
    }
    val request = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .build()
    connectivityManager.registerNetworkCallback(request, networkCallback)

    awaitClose { connectivityManager.unregisterNetworkCallback(networkCallback) }
}
