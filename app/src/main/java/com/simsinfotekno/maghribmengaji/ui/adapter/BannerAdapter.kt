package com.simsinfotekno.maghribmengaji.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.simsinfotekno.maghribmengaji.databinding.ItemBannerBinding


class BannerAdapter(
//    private val context: Context,
    private val bannerList: List<Int>,
) : RecyclerView.Adapter<BannerAdapter.BannerHolder>() {

    // Set val to infinite banner scroll
    private val newBanner: List<Int> = listOf(bannerList.last()) + bannerList + listOf(bannerList.first())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerHolder {
//        val binding = ItemBannerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//        return BannerHolder(binding)
        return BannerHolder.from(parent)
    }

    override fun onBindViewHolder(holder: BannerHolder, position: Int) {
        val banner = newBanner[position]
        holder.bind(banner)
    }

    override fun getItemCount(): Int {
        return newBanner.size
    }

    class BannerHolder(private var itemHolderBinding: ItemBannerBinding) :
        RecyclerView.ViewHolder(itemHolderBinding.root) {
        fun bind(banner: Int) {
            itemHolderBinding.itemBannerImageViewBanner.setImageResource(banner)
        }

        companion object {
            fun from(parent: ViewGroup) : BannerHolder {
                val itemView = ItemBannerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return BannerHolder(itemView)
            }
        }
    }
}


