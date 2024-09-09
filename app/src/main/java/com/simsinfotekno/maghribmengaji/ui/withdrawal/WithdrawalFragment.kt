package com.simsinfotekno.maghribmengaji.ui.withdrawal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.simsinfotekno.maghribmengaji.MainApplication
import com.simsinfotekno.maghribmengaji.MainApplication.Companion.transactionRepository
import com.simsinfotekno.maghribmengaji.R
import com.simsinfotekno.maghribmengaji.databinding.FragmentBalanceBinding
import com.simsinfotekno.maghribmengaji.ui.adapter.TransactionAdapter
import com.simsinfotekno.maghribmengaji.usecase.FormatToIndonesianCurrencyUseCase
import com.simsinfotekno.maghribmengaji.usecase.TransactionService

class WithdrawalFragment : Fragment() {

    private lateinit var binding: FragmentBalanceBinding

    companion object {
        fun newInstance() = WithdrawalFragment()
    }

    private val viewModel: WithdrawalViewModel by viewModels()

    /* Variables */
    private var isBalanceVisible: Boolean = false
    private var balance: Int? = null
    private lateinit var adapter: TransactionAdapter

    /* Use cases */
    private val formatToIndonesianCurrencyUseCase = FormatToIndonesianCurrencyUseCase()
    private val transactionService = TransactionService()

    // Show/hide balance
    private fun toggleVisibility() {
        if (isBalanceVisible) {
            binding.balanceValue.text = formatToIndonesianCurrencyUseCase(balance)
            binding.balanceVisibilityButton.setIconResource(R.drawable.outline_visibility_off_24)
            isBalanceVisible = true
        } else {
            isBalanceVisible = false
            binding.balanceValue.text = "***"
            binding.balanceVisibilityButton.setIconResource(R.drawable.outline_visibility_24)
            binding.balanceValue.text = "***"
        }
        isBalanceVisible = !isBalanceVisible
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBalanceBinding.inflate(inflater, container, false)

        val user = MainApplication.studentRepository.getStudent() // Get current user
        balance = transactionRepository.getBalance() // Get user's balance

        /* Views */
        toggleVisibility() // Hide balance
        setupRecyclerView()
        if (balance == null || balance == 0) {
            binding.balanceTextHistory.visibility = View.GONE
            binding.balanceWithdrawButton.isEnabled = false
        }

        /* Listeners */
        binding.balanceToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.balanceVisibilityButton.setOnClickListener {
            toggleVisibility()
        }
        binding.balanceWithdrawButton.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(resources.getString(R.string.are_you_sure))
                .setMessage(getString(R.string.do_you_wish_to_withdraw_all_your_money))
                .setPositiveButton(resources.getString(R.string.okay)) { dialog, _ ->
                    // Process withdrawal
                    balance?.let { balance ->
                        transactionService.withdraw(balance) {
                            it.onSuccess {

                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.withdrawal_request_successfully_sent),
                                    Toast.LENGTH_LONG
                                )
                                    .show()

                                adapter.notifyDataSetChanged()

                                //                                findNavController().popBackStack()

                            }.onFailure { exception ->
                                Toast.makeText(
                                    requireContext(),
                                    exception.localizedMessage,
                                    Toast.LENGTH_LONG
                                )
                                    .show()
                            }
                        }
                    }

                    dialog.dismiss() // Close dialog
                }
                .setNeutralButton(getString(R.string.cancel), null)
                .show()
        }
        binding.balanceDepositButton.setOnClickListener {
            transactionService.deposit(1000000000) {
                it.onSuccess { transaction ->

                    Toast.makeText(
                        requireContext(),
                        "Deposited ${formatToIndonesianCurrencyUseCase(transaction.amount)} successfully",
                        Toast.LENGTH_LONG
                    )
                        .show()

                    adapter.notifyDataSetChanged()

                }.onFailure { exception ->
                    Toast.makeText(
                        requireContext(),
                        exception.localizedMessage,
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
            }
        }

        return binding.root
    }

    private fun setupRecyclerView() {
        adapter = TransactionAdapter(transactionRepository.getRecords()) { transaction ->
            //
        }
        binding.balanceTransactionRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.balanceTransactionRecyclerView.adapter = adapter
    }

}