package com.simsinfotekno.maghribmengaji.ui.page

import android.app.Activity
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.simsinfotekno.maghribmengaji.MainActivity
import com.simsinfotekno.maghribmengaji.R
import com.simsinfotekno.maghribmengaji.databinding.FragmentPageBinding
import com.simsinfotekno.maghribmengaji.usecase.LaunchScannerUseCase
import com.simsinfotekno.maghribmengaji.usecase.UploadImageUseCase

class PageFragment : Fragment(), ActivityResultCallback<ActivityResult> {

    companion object {
        private val TAG = PageFragment::class.java.simpleName
        fun newInstance() = PageFragment()
    }

    private val viewModel: PageViewModel by viewModels()

    private var _binding: FragmentPageBinding? = null
    private val binding get() = _binding!!

    // Repository
    private val quranPageRepository = MainActivity.quranPageRepository
    private val quranVolumeRepository = MainActivity.quranVolumeRepository
    private val quranPageStudentRepository = MainActivity.quranPageStudentRepository

    // State

    // Use case
    private val uploadImageUseCase = UploadImageUseCase()
    private val launchScannerUseCase = LaunchScannerUseCase()

    private val PICK_IMAGE_REQUEST = 1
    private var pageId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPageBinding.inflate(inflater, container, false)

        pageId = arguments?.getInt("pageId")
        val page =
            quranPageRepository.getRecordById(pageId) // Get QuranPage instance
        val volume =
            quranVolumeRepository.getRecordByPageId(page!!.id) // Get QuranVolume instance
        val pageStudent =
            quranPageStudentRepository.getRecordByPageId(pageId) // Get student's page instance if any

        // Initialize the scanner launcher
        val scannerLauncher = registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult(),
            this
        )

        // View
        binding.pageTextViewVolume.text =
            getString(R.string.quran_volume, volume?.id.toString())
        binding.pageTextViewPage.text = getString(R.string.quran_page, pageId.toString())

        if (pageStudent == null) {
            // If student's page is not found or student had not submitted

            binding.pageTextViewScore.visibility = View.GONE // Hide score
            binding.pageButtonSubmit.visibility = View.VISIBLE // Show submit button
        }
        else {
            // If student had submitted

            binding.pageTextViewScore.visibility = View.VISIBLE // Show score
            binding.pageButtonSubmit.visibility = View.GONE // Show submit button
        }



        // Get the document from Firestore and load the image to ImageView
        val pageImage = binding.pageImageViewPage

        quranPageRepository.getFirebaseRecordById(pageId!!,
            { imageUrl ->

                // Load image to ImageView
                loadImageIntoImageView(imageUrl, pageImage)

            },
            { exception ->

                Toast.makeText(
                    requireContext(),
                    "Failed to load image: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
                binding.pageProgressBar.visibility = View.GONE // Hide progress bar

            })

        binding.pageButtonForward.setOnClickListener {


        }

        binding.pageButtonPrevious.setOnClickListener {

        }

        // Submit button
        binding.pageButtonSubmit.setOnClickListener {

            LaunchScannerUseCase().invoke(this, scannerLauncher)

            // Browse image to upload TODO: Move to use case
//            val intent = Intent(Intent.ACTION_GET_CONTENT)
//            intent.type = "image/*"
//            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        return binding.root
    }

    // Load image to imageview
    private fun loadImageIntoImageView(imageUrl: String, imageView: ImageView) {
        binding.pageProgressBar.visibility = View.VISIBLE

        Glide.with(imageView.context)
            .load(imageUrl)
            .listener(object : RequestListener<Drawable> {

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.pageProgressBar.visibility = View.GONE
                    imageView.visibility = View.VISIBLE
                    return false
                }

                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.pageProgressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Failed to load image", Toast.LENGTH_SHORT)
                        .show()
                    return false
                }
            })
            .into(imageView)
    }

    // Get image file
//    @Deprecated("Deprecated in Java")
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
//            val fileUri = data?.data
//
//            // Upload image to Firebase storage TODO: Move to use case
//            fileUri?.let {
//                uploadImageUseCase(pageId!!, it, {
//                    Toast.makeText(
//                        requireContext(),
//                        "Image uploaded successfully",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }, { exception ->
//                    Toast.makeText(
//                        requireContext(),
//                        "Failed to upload image: ${exception.message}",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                })
//            } ?: Toast.makeText(requireContext(), "No file selected", Toast.LENGTH_SHORT).show()
//
//        }
//    }

    override fun onActivityResult(result: ActivityResult) {
        val resultCode = result.resultCode
        val resultX = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
        if (resultCode == Activity.RESULT_OK && resultX != null) {
            val pages = resultX.pages
            if (!pages.isNullOrEmpty()) {

                // Bundle to pass the data
                val bundle = Bundle().apply {
                    putString("image_uri", pages[0].imageUri.toString())
                    putInt("pageId", pageId!!)
                }

                // Navigate to the ResultFragment with the Bundle
                findNavController().navigate(
                    R.id.action_pageFragment_to_similarityScoreFragment,
                    bundle
                )

            }

        } else if (resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(
                requireContext(),
                getString(R.string.error_scanner_cancelled),
                Toast.LENGTH_LONG
            ).show()
        } else {
            Toast.makeText(
                requireContext(),
                getString(R.string.error_default_message),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Handling activity result
    private fun handleActivityResult(activityResult: ActivityResult) {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}