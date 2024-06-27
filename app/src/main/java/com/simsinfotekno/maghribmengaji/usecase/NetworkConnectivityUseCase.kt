package com.simsinfotekno.maghribmengaji.usecase

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.simsinfotekno.maghribmengaji.NetworkConnectivityObserver
import com.simsinfotekno.maghribmengaji.enums.ConnectivityObserver
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class NetworkConnectivityUseCase(private val context: Context) {
    private val connectivityObserver = NetworkConnectivityObserver(context)
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private var lastStatus: ConnectivityObserver.Status = ConnectivityObserver.Status.Available

    companion object {
        private val TAG = NetworkConnectivityUseCase::class.java.simpleName
    }

    operator fun invoke(
        lifecycleOwner: LifecycleOwner,
        onAvailableNetwork: () -> Unit = {},
        onUnavailableNetwork: () -> Unit = {},
//        onLosing: () -> Unit = {},
//        onLost: () -> Unit = {}
    ) {
        connectivityObserver.observe().onEach {
            when (it) {
                ConnectivityObserver.Status.Available -> {
                    lastStatus = ConnectivityObserver.Status.Available
                    Log.d(TAG, "Internet Available")
                    onAvailableNetwork()
                }

                else -> {
                    lastStatus = ConnectivityObserver.Status.Unavailable
                    Log.d(TAG, "Internet Unavailable")
                    onUnavailableNetwork()
                }
//                ConnectivityObserver.Status.Losing -> {
//                    Log.d(TAG, "Internet Losing")
//                    onLosing()
//                }
//                ConnectivityObserver.Status.Lost -> {
//                    Log.d(TAG, "Internet Lost")
//                    onLost()
//                }
            }
//            Toast.makeText(context, "Internet status $it", Toast.LENGTH_SHORT).show()
        }.launchIn(lifecycleOwner.lifecycleScope)

        // Check the current connection status immediately
        val currentStatus = getCurrentNetworkStatus()
        if (lastStatus != currentStatus) {
            when (currentStatus) {
                ConnectivityObserver.Status.Available -> onAvailableNetwork()
                else -> onUnavailableNetwork()
            }
        }
//        lifecycleOwner.lifecycleScope.launch {
//            connectivityObserver.observe().collect {
//                when (it) {
//                    ConnectivityObserver.Status.Available -> {
//                        Log.d(TAG, "Internet Available")
//                        onAvailableNetwork()
//                    }
//
//                    else -> {
//                        Log.d(TAG, "Internet Unavailable")
//                        onUnavailableNetwork()
//                    }
//                }
//            }
//        }
    }

    private fun getCurrentNetworkStatus(): ConnectivityObserver.Status {
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return when {
            capabilities == null -> ConnectivityObserver.Status.Unavailable
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) -> ConnectivityObserver.Status.Available
            else -> ConnectivityObserver.Status.Unavailable
        }
    }
}