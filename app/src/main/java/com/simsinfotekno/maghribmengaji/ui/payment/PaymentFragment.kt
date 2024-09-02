package com.simsinfotekno.maghribmengaji.ui.payment

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.SkuDetails
import com.android.billingclient.api.SkuDetailsParams
import com.simsinfotekno.maghribmengaji.R
import com.simsinfotekno.maghribmengaji.databinding.FragmentPaymentBinding
import com.simsinfotekno.maghribmengaji.databinding.FragmentSettingBinding

class PaymentFragment : Fragment(){

    companion object {
        fun newInstance() = PaymentFragment()
    }
    private val viewModel: PaymentViewModel by viewModels()
    private lateinit var binding: FragmentPaymentBinding
    private lateinit var billingClient: BillingClient
    private lateinit var skuDetails: SkuDetails

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Sama seperti di atas, inisialisasi BillingClient dan query produk
        BillingClient.newBuilder(requireContext())
            .setListener { billingResult, purchases ->
                handlePurchases(purchases)
            }
            .enablePendingPurchases()
            .build().also { billingClient = it }

        startBillingConnection()

        // Inisialisasi tombol pembelian
        view.findViewById<Button>(R.id.buyButton).setOnClickListener {
            launchPurchaseFlow(skuDetails)
        }
    }

    private fun startBillingConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryAvailableProducts()  // Query produk setelah billing setup selesai
                }
            }

            override fun onBillingServiceDisconnected() {
                // Coba untuk memulai kembali koneksi jika terputus
            }
        })
    }

    private fun queryAvailableProducts() {
        val skuList = listOf("your_product_id")  // ID produk dari Google Play Console
        val params = SkuDetailsParams.newBuilder()
            .setSkusList(skuList)
            .setType(BillingClient.SkuType.INAPP)
            .build()

        billingClient.querySkuDetailsAsync(params) { billingResult, skuDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && skuDetailsList != null) {
                // Tampilkan produk kepada pengguna
                // Misalnya, update UI dengan informasi produk
            }
        }
    }

    private fun launchPurchaseFlow(skuDetails: SkuDetails) {
        val flowParams = BillingFlowParams.newBuilder()
            .setSkuDetails(skuDetails)
            .build()

        billingClient.launchBillingFlow(requireActivity(), flowParams)
    }

    private fun handlePurchases(purchases: MutableList<Purchase>?) {
        if (purchases != null) {
            for (purchase in purchases) {
                // Implementasi logika pembelian
                // Misalnya, tambahkan poin ke akun pengguna
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        billingClient.endConnection()
    }


}


