package com.simsinfotekno.maghribmengaji

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.simsinfotekno.maghribmengaji.enums.ConnectivityObserver

class MainViewModel : ViewModel() {

    private val _connectionStatus = MutableLiveData<ConnectivityObserver.Status>()
    val connectionStatus: LiveData<ConnectivityObserver.Status> get() = _connectionStatus

    fun networkAvailable() {
        _connectionStatus.value = ConnectivityObserver.Status.Available
    }
    fun networkUnavailable() {
        _connectionStatus.value = ConnectivityObserver.Status.Unavailable
    }
    fun logout() {
        Firebase.auth.signOut()
    }
}