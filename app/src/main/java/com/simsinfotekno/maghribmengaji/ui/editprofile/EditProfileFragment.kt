package com.simsinfotekno.maghribmengaji.ui.editprofile

import android.content.Context
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.provider.Settings.Secure.putString
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.simsinfotekno.maghribmengaji.R

class EditProfileFragment : Fragment() {

    companion object {
        fun newInstance() = EditProfileFragment()
    }

    private val viewModel: EditProfileViewModel by viewModels()
    private lateinit var InputNama: EditText
    private lateinit var InputAlamatRumah: EditText
    private lateinit var InputNamaSekolah: EditText
    private lateinit var InputAlamatSekolah: EditText
    private lateinit var InputBank: EditText
    private lateinit var InputNoRekening: EditText
    private lateinit var btn_save: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        InputNama = view?.findViewById(R.id.InputNama)!!
        InputAlamatRumah = view?.findViewById(R.id.InputAlamatRumah)!!
        InputNamaSekolah = view?.findViewById(R.id.InputNamaSekolah)!!
        InputAlamatSekolah = view?.findViewById(R.id.InputAlamatSekolah)!!
        InputBank = view?.findViewById(R.id.InputBank)!!
        InputNoRekening = view?.findViewById(R.id.InputNoRekening)!!

        btn_save.setOnClickListener {
            //saveProfileData()
        }
        return inflater.inflate(R.layout.fragment_edit_profile, container, false)
    }
//    private fun saveProfileData() {}
//    val saveString = requireContext().getSharedPreferences("UserProfile", Context.MODE_PRIVATE)
//    with(sharedPrefs.edit()){
//        putString("nama", InputNama.text.toString())
//        putString("alamatRumah", InputAlamatRumah.text.toString())
//        putString("namaSekolah", InputNamaSekolah.text.toString())
//        putString("alamatSekolah", InputAlamatSekolah.text.toString())
//        putString("bank", InputBank.text.toString())
//        putString("noRekening", InputNoRekening.text.toString())
//        apply()
//    }
//        requireActivity().supportFragmentManager.beginTransaction()
//            .replace(R.id.fragment_container, ProfileDisplayFragment())
//            .commit()
//    }

}