package com.simsinfotekno.maghribmengaji.usecase

import android.content.Context
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.simsinfotekno.maghribmengaji.NetworkConnectivityObserver
import com.simsinfotekno.maghribmengaji.enums.ConnectivityObserver
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class NetworkConnectivityUseCase(private val context: Context) {
    private val connectivityObserver = NetworkConnectivityObserver(context)

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
                    Log.d(TAG, "Internet Available")
                    onAvailableNetwork()
                }
                else -> {
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
}