package com.simsinfotekno.maghribmengaji.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.simsinfotekno.maghribmengaji.R
import com.simsinfotekno.maghribmengaji.databinding.ItemTransactionBinding
import com.simsinfotekno.maghribmengaji.model.MaghribMengajiTransaction
import com.simsinfotekno.maghribmengaji.usecase.FormatToIndonesianCurrencyUseCase
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionAdapter(
    private val infaqList: List<MaghribMengajiTransaction>,
    private val onClick: (MaghribMengajiTransaction) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    /* Variables */
    // Define a localized date-time format
    val dateFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm:ss", Locale.getDefault())

    /* Use cases */
    private val formatToIndonesianCurrencyUseCase = FormatToIndonesianCurrencyUseCase()

    inner class TransactionViewHolder(val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(transaction: MaghribMengajiTransaction) {
            binding.itemTransactionTitle.text =
                formatToIndonesianCurrencyUseCase(transaction.amount)
            binding.itemTransactionType.text = transaction.type?.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.ROOT
                ) else it.toString()
            }
            binding.itemTransactionDate.text = transaction.createdAt?.toDate()
                ?.let { dateFormat.format(it) }

            if (transaction.type != MaghribMengajiTransaction.TYPE_DEPOSIT)
                binding.itemTransactionTypeImage.setColorFilter(
                    binding.itemTransactionTypeImage.context.getColor(
                        R.color.md_theme_error
                    )
                )

            binding.itemTransactionTypeImage.setImageResource(
                when (transaction.type) {
                    MaghribMengajiTransaction.TYPE_DEPOSIT ->
                        R.drawable.round_add_24

                    MaghribMengajiTransaction.TYPE_WITHDRAWAL ->
                        R.drawable.round_remove_24

                    MaghribMengajiTransaction.TYPE_PAYMENT ->
                        R.drawable.round_remove_24

                    else -> {
                        R.drawable.round_remove_24
                    }
                }


            )

            binding.itemTransactionCardView.setOnClickListener {
                onClick(transaction)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding =
            ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(infaqList[position])
    }

    override fun getItemCount() = infaqList.size
}
