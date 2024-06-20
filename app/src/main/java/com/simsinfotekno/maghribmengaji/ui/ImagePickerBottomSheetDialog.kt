package com.simsinfotekno.maghribmengaji.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.simsinfotekno.maghribmengaji.databinding.BottomSheetDialogBinding

class ImagePickerBottomSheetDialog : BottomSheetDialogFragment() {

    private var _binding: BottomSheetDialogBinding? = null
    private val binding get() = _binding!!

    var onCameraClick: (() -> Unit)? = null
    var onGalleryClick: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = BottomSheetDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCamera.setOnClickListener {
            onCameraClick?.invoke()
            dismiss()
        }

        binding.btnGallery.setOnClickListener {
            onGalleryClick?.invoke()
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "ImagePickerBottomSheetDialog"
    }
}