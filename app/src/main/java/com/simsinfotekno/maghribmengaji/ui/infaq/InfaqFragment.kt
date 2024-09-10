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
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.google.common.collect.ImmutableList
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
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts()
                    .build()
            )
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
        infaqAdapter = InfaqAdapter(infaqList) { productDetails ->
            launchPurchaseFlow(productDetails)
        }
        binding.donationRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.donationRecyclerView.adapter = infaqAdapter

        // Setup test datasets
//        infaqList.add(Infaq("infaq_5000", "5000", "Infaq ${formatToIndonesianCurrencyUseCase(5000)}"))
//        infaqList.add(Infaq("infaq_10000", "10000", "Infaq ${formatToIndonesianCurrencyUseCase(10000)}"))
//        infaqList.add(Infaq("infaq_15000", "15000", "Infaq ${formatToIndonesianCurrencyUseCase(15000)}"))
//        infaqList.add(Infaq("infaq_20000", "15000", "Infaq ${formatToIndonesianCurrencyUseCase(20000)}"))
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
        val productList = ImmutableList.of(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("infaq_5000")
                .setProductType(BillingClient.ProductType.INAPP)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("infaq_10000")
                .setProductType(BillingClient.ProductType.INAPP)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("infaq_15000")
                .setProductType(BillingClient.ProductType.INAPP)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("infaq_20000")
                .setProductType(BillingClient.ProductType.INAPP)
                .build(),
        ) // Replace with actual product IDs from the Google Play Console

        val queryProductDetailsParams =
            QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build()

        billingClient.queryProductDetailsAsync(queryProductDetailsParams) { billingResult, productDetailsList ->

            Log.d(TAG, "Billing result: $billingResult | Product Details: $productDetailsList")

            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {

                val sortedProductDetailsList = productDetailsList.sortedBy {
                    it.oneTimePurchaseOfferDetails?.priceAmountMicros ?: Long.MAX_VALUE
                } // Products sorted by price
                val infaqList = mutableListOf<Infaq>() // Initialize container for products list

                sortedProductDetailsList.forEachIndexed { index, productDetails ->
                    val infaq = Infaq(
                        sku = productDetails.productId,
                        price = productDetails.oneTimePurchaseOfferDetails?.formattedPrice,
                        title = productDetails.name,
                        productDetails = productDetails
                    )
                    infaqList.add(infaq)
                    Log.d(TAG, "Product: $productDetails")
                }

                Log.d(TAG, "infaqAdapter updated")

                // Now you can use infaqList, which contains all Infaq objects created from productDetailsList
                Log.d(TAG, "Infaq List: $infaqList")

                requireActivity().runOnUiThread {
                    infaqAdapter.infaqList = infaqList
                    infaqAdapter.notifyDataSetChanged()
                    binding.donationProgressIndicator.visibility =
                        View.GONE // Hide progress indicator
                }

            }

        }

    }


    private fun launchPurchaseFlow(productDetails: ProductDetails) {
//        val flowParams = BillingFlowParams.newBuilder()
//            .setSkuDetails(productDetails)
//            .build()
//
//        billingClient.launchBillingFlow(requireActivity(), flowParams)
//////////
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                .setProductDetails(productDetails)
                // For One-time product, "setOfferToken" method shouldn't be called.
                // For subscriptions, to get an offer token, call ProductDetails.subscriptionOfferDetails()
                // for a list of offers that are available to the user
//                .setOfferToken(selectedOfferToken)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        // Launch the billing flow
        billingClient.launchBillingFlow(requireActivity(), billingFlowParams)
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


