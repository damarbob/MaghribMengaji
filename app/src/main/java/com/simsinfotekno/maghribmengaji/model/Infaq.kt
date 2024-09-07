package com.simsinfotekno.maghribmengaji.model

import com.android.billingclient.api.SkuDetails

data class Infaq(
    val sku: String,
    val price: String,
    val title: String,
    val skuDetails: SkuDetails? = null // Optional, will be set after querying SKU details
)
