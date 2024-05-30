package com.simsinfotekno.maghribmengaji.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.simsinfotekno.maghribmengaji.MainActivity
import com.simsinfotekno.maghribmengaji.R
import com.simsinfotekno.maghribmengaji.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding

    companion object {
        fun newInstance() = LoginFragment()
    }

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(layoutInflater)

        /* Observers */
        viewModel.loginResult.observe(viewLifecycleOwner, Observer { result ->
            result?.onSuccess {
                // Navigate to the next screen or update UI
                Toast.makeText(requireContext(), "Welcome back, ${it.displayName}", Toast.LENGTH_SHORT).show()

                val intent = Intent(activity, MainActivity::class.java)
                startActivity(intent)
                activity?.finish()  // Finish the current activity so the user can't navigate back to the login screen

            }?.onFailure { exception ->
                // Show error message
                Toast.makeText(requireContext(), exception.message, Toast.LENGTH_SHORT).show()
            }
        })

        /* Listeners */
        binding.loginButton.setOnClickListener {
            val email = binding.loginInputEmailAddress.text.toString()
            val password = binding.loginInputPassword.text.toString()
            viewModel.loginWithEmailPassword(email, password)
        }
        binding.loginSignUpText.setOnClickListener{
            findNavController().navigate(R.id.action_loginFragment_to_signUpFragment)
        }

        return binding.root
    }
}