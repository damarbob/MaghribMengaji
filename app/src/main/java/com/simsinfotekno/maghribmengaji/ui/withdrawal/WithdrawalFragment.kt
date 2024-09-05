package com.simsinfotekno.maghribmengaji.ui.withdrawal

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.simsinfotekno.maghribmengaji.R

class WithdrawalFragment : Fragment() {

    companion object {
        fun newInstance() = WithdrawalFragment()
    }

    private val viewModel: WithdrawalViewModel by viewModels()
    private lateinit var tv_balance: TextView
    private lateinit var btn_img: ImageView
    private var isBalanceVisible: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //hide balance
        tv_balance = view?.findViewById(R.id.tv_balance)!!
        btn_img = view?.findViewById(R.id.btn_img)!!
        btn_img.setOnClickListener {
            toggleVisibility()
        }
    }
    //hide balance
    private fun toggleVisibility() {
        if (isBalanceVisible) {
            tv_balance.visibility = View.GONE
            btn_img.setImageResource(R.drawable.visibility)
        } else {
            tv_balance.visibility = View.VISIBLE
            btn_img.setImageResource(R.drawable.visibility_off)
        }
        isBalanceVisible = !isBalanceVisible
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_withdrawal, container, false)
    }
}