package com.simsinfotekno.maghribmengaji.usecase

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.ContextCompat.getString
import com.simsinfotekno.maghribmengaji.R

class OpenWhatsApp {

    // TODO: add country code
    operator fun invoke(
        context: Context,
        phoneNumber: String,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = { e ->
            Toast.makeText(context, context.getString(R.string.error), Toast.LENGTH_SHORT).show()
        }
    ) {
        val formattedNumber = phoneNumber.replace("+", "").replace(" ", "")
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("https://wa.me/$formattedNumber")

        try {
            context.startActivity(intent)
            onSuccess()
        } catch (e: Exception) {
            onFailure(e)
        }
    }
}