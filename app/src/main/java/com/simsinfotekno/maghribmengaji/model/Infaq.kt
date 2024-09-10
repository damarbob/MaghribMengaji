package com.simsinfotekno.maghribmengaji.model

import com.android.billingclient.api.ProductDetails

data class Infaq(
    val sku: String,
    val price: String? = null,
    val title: String,
    val productDetails: ProductDetails? = null // Optional, will be set after querying SKU details
)
