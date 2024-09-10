package com.simsinfotekno.maghribmengaji.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.ProductDetails
import com.simsinfotekno.maghribmengaji.databinding.ItemInfaqBinding
import com.simsinfotekno.maghribmengaji.model.Infaq
import com.simsinfotekno.maghribmengaji.usecase.FormatToIndonesianCurrencyUseCase

class InfaqAdapter(
    var infaqList: List<Infaq>,
    private val onPurchaseClick: (ProductDetails) -> Unit
) : RecyclerView.Adapter<InfaqAdapter.InfaqViewHolder>() {

    /* Use case */
    private val formatToIndonesianCurrencyUseCase = FormatToIndonesianCurrencyUseCase()

    inner class InfaqViewHolder(val binding: ItemInfaqBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(infaq: Infaq) {
            binding.infaqTitle.text = infaq.title
            binding.infaqPrice.text = infaq.price
            binding.itemInfaqCardView.setOnClickListener {
                infaq.productDetails?.let { productDetails -> onPurchaseClick(productDetails) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InfaqViewHolder {
        val binding = ItemInfaqBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return InfaqViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InfaqViewHolder, position: Int) {
        holder.bind(infaqList[position])
    }

    override fun getItemCount() = infaqList.size

}
