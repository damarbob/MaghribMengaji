package com.simsinfotekno.maghribmengaji.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.SkuDetails
import com.simsinfotekno.maghribmengaji.databinding.ItemInfaqBinding
import com.simsinfotekno.maghribmengaji.model.Infaq

class InfaqAdapter(
    private val infaqList: List<Infaq>,
    private val onPurchaseClick: (SkuDetails) -> Unit
) : RecyclerView.Adapter<InfaqAdapter.InfaqViewHolder>() {

    inner class InfaqViewHolder(val binding: ItemInfaqBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(infaq: Infaq) {
            binding.infaqTitle.text = infaq.title
            binding.infaqPrice.text = infaq.price
            binding.itemInfaqCardView.setOnClickListener {
                infaq.skuDetails?.let { skuDetails -> onPurchaseClick(skuDetails) }
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
