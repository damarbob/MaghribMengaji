package com.simsinfotekno.maghribmengaji.ui.infaq

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.SkuDetails
import com.android.billingclient.api.SkuDetailsParams
import com.simsinfotekno.maghribmengaji.databinding.FragmentInfaqBinding
import com.simsinfotekno.maghribmengaji.model.Infaq
import com.simsinfotekno.maghribmengaji.ui.adapter.InfaqAdapter

// TODO: Test functionality
class InfaqFragment : DialogFragment() {

    private lateinit var binding: FragmentInfaqBinding

    companion object {
        fun newInstance() = InfaqFragment()
        private val TAG = InfaqFragment::class.simpleName
    }

    /* View models */
    private val viewModel: InfaqViewModel by viewModels()

    /* Variables */
    private lateinit var billingClient: BillingClient

    /* Adapters */
    private lateinit var infaqAdapter: InfaqAdapter
    private val infaqList = mutableListOf<Infaq>() // List to hold the in-app products

    override fun onStart() {
        super.onStart()

        // Fullscreen
        val dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window!!.setLayout(width, height)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentInfaqBinding.inflate(inflater, container, false)

        // Sama seperti di atas, inisialisasi BillingClient dan query produk
        BillingClient.newBuilder(requireContext())
            .setListener { billingResult, purchases ->
                handlePurchases(purchases)
            }
            .enablePendingPurchases()
            .build().also { billingClient = it }

        startBillingConnection()

        // Initialize infaq button
//        binding.donationButton5000.setText(getString(R.string.infaq_x, "IDR 5.000"))
//        binding.donationButton10000.setText(getString(R.string.infaq_x, "IDR 10.000"))
//        binding.donationButton15000.setText(getString(R.string.infaq_x, "IDR 15.000"))
//        binding.donationButton20000.setText(getString(R.string.infaq_x, "IDR 20.000"))

        setupRecyclerView()

        /* Listeners */
        binding.donationToolbar.setNavigationOnClickListener {
//            findNavController().popBackStack()
            dismiss()
        }

        return binding.root
    }

    private fun setupRecyclerView() {
        infaqAdapter = InfaqAdapter(infaqList) { skuDetails ->
            launchPurchaseFlow(skuDetails)
        }
        binding.donationRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.donationRecyclerView.adapter = infaqAdapter

        // Setup test datasets
//        infaqList.add(Infaq("infaq_5000", "5000", "Infaq 5000"))
//        infaqList.add(Infaq("infaq_10000", "10000", "Infaq 10000"))
//        infaqList.add(Infaq("infaq_15000", "15000", "Infaq 15000"))
        infaqAdapter.notifyDataSetChanged()
    }

    private fun startBillingConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryAvailableProducts()  // Query available products after finished setup
                }
            }

            override fun onBillingServiceDisconnected() {
                // Retry connection or handle the error
                Log.e(TAG, "Billing Service Disconnected")
                binding.donationProgressIndicator.visibility = View.GONE // Hide progress indicator on error
            }
        })
    }

    private fun queryAvailableProducts() {
        val skuList = listOf(
            "infaq_5000",
            "infaq_10000",
            "infaq_15000",
            "infaq_20000"
        ) // Replace with actual product IDs from the Google Play Console
        val params = SkuDetailsParams.newBuilder()
            .setSkusList(skuList)
            .setType(BillingClient.SkuType.INAPP)
            .build()

        billingClient.querySkuDetailsAsync(params) { billingResult, skuDetailsList ->

            Log.d(TAG, "Billing result: $billingResult | SKU Details: $skuDetailsList")

            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && skuDetailsList != null) {

                binding.donationProgressIndicator.visibility = View.GONE // Hide progress indicator

                for (skuDetail in skuDetailsList) {

                    val infaq = Infaq(
                        sku = skuDetail.sku,
                        price = skuDetail.price,
                        title = skuDetail.title,
                        skuDetails = skuDetail
                    )
                    infaqList.add(infaq)

                    infaqAdapter.notifyDataSetChanged()

                    // Old
                    when (skuDetail.sku) {
//                        "infaq_5000" -> binding.donationButton5000.setOnClickListener { launchPurchaseFlow(skuDetail) }
//                        "infaq_10000" -> binding.donationButton10000.setOnClickListener { launchPurchaseFlow(skuDetail) }
//                        "infaq_15000" -> binding.donationButton15000.setOnClickListener { launchPurchaseFlow(skuDetail) }
//                        "infaq_20000" -> binding.donationButton20000.setOnClickListener { launchPurchaseFlow(skuDetail) }
                    }
                }
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
        purchases?.forEach { purchase ->
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                // Acknowledge the purchase if it hasn't been acknowledged
                if (!purchase.isAcknowledged) {
                    val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()
                    billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                            // Purchase acknowledged
                        }
                    }
                }
                // Additional logic to grant the user the purchased item
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        billingClient.endConnection()
    }


}


