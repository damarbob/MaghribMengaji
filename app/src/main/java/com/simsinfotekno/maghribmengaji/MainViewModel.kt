package com.simsinfotekno.maghribmengaji

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainViewModel : ViewModel() {
    fun logout() {
        Firebase.auth.signOut()
    }
}