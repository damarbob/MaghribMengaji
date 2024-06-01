package com.simsinfotekno.maghribmengaji.ui.audioplayer

import android.app.Activity
import android.content.Intent
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.google.android.material.transition.MaterialSharedAxis
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.simsinfotekno.maghribmengaji.R
import com.simsinfotekno.maghribmengaji.databinding.FragmentAudioPlayerBinding
import java.io.File
import java.io.IOException

class AudioPlayerFragment : Fragment() {

    companion object {
        fun newInstance() = AudioPlayerFragment()
        private const val PICK_AUDIO_REQUEST_CODE = 1001
        private val TAG = AudioPlayerFragment::class.java.simpleName
    }

    private var _binding: FragmentAudioPlayerBinding? = null

    private val binding get() = _binding!!

    private val viewModel: AudioPlayerViewModel by viewModels()

    private var mediaPlayer: MediaPlayer? = null
    private var outputFile: File? = null
    private var selectedAudioUri: Uri? = null
    private var handler = Handler(Looper.getMainLooper())
    private var isPaused = false
    private var pauseTime: Long = 0

    private val updatePlayTimeThread = object : Runnable {
        override fun run() {
            mediaPlayer?.let {
                val timeInMilliseconds = if (isPaused) {
                    pauseTime
                } else {
                    it.currentPosition.toLong()
                }
                val seconds = (timeInMilliseconds / 1000).toInt() % 60
                val minutes = (timeInMilliseconds / (1000 * 60) % 60).toInt()
                binding.audioPlayerTextViewTime.text = String.format("%02d:%02d", minutes, seconds)
                handler.postDelayed(this, 1000)
            }
        }
    }

    private var pageId: Int? = -1
    private var studentId: String = Firebase.auth.currentUser!!.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the transition for this fragment
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ false)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ true)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ false)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAudioPlayerBinding.inflate(inflater, container, false)

        pageId = arguments?.getInt("pageId")
        Log.d(TAG, pageId.toString())

        // Use FileProvider to get the URI of the recorded file
        outputFile = File(requireContext().externalCacheDir?.absolutePath, "audio record page $pageId.webm")
        selectedAudioUri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", outputFile!!)

        binding.audioPlayerTextViewPage.text = getString(R.string.audio_page, pageId.toString())
        binding.audioPlayerButtonPause.visibility = View.GONE

        binding.audioPlayerButtonPlay.setOnClickListener {
            playRecording()
        }

        binding.audioPlayerButtonPause.setOnClickListener {
            pauseRecording()
        }

        binding.audioPlayerButtonUpload.setOnClickListener {
            uploadRecording()
        }

        binding.audioPlayerButtonPick.setOnClickListener {
            pickAudioFromStorage()
        }

        return binding.root
    }

    private fun playRecording() {
        if (selectedAudioUri == null) {
            Toast.makeText(context, "No audio selected", Toast.LENGTH_SHORT).show()
            return
        }

        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer().apply {
                try {
                    setDataSource(requireContext(), selectedAudioUri!!)
                    prepare()
                    start()
                    handler.postDelayed(updatePlayTimeThread, 0)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        } else {
            mediaPlayer?.start()
            handler.postDelayed(updatePlayTimeThread, 0)
        }

        isPaused = false
        binding.audioPlayerButtonPlay.visibility = View.GONE
        binding.audioPlayerButtonPause. visibility = View.VISIBLE
    }

    private fun pauseRecording() {
        mediaPlayer?.pause()
        isPaused = true
        pauseTime = mediaPlayer?.currentPosition?.toLong() ?: 0
        handler.removeCallbacks(updatePlayTimeThread)
        binding.audioPlayerButtonPlay.visibility = View.VISIBLE
        binding.audioPlayerButtonPause. visibility = View.GONE
    }

    private fun uploadRecording() {
        if (selectedAudioUri == null) {
            Toast.makeText(context, "No audio selected", Toast.LENGTH_SHORT).show()
            return
        }

        getAudioRecord()

    }

    private fun uploadAndSaveAudio() {
        val file = selectedAudioUri!!
        val storageReference = FirebaseStorage.getInstance().reference.child("audio/${studentId}/${pageId}")
        val uploadTask = storageReference.putFile(file)

        uploadTask.addOnSuccessListener {
            storageReference.downloadUrl.addOnSuccessListener { uri ->
                val db = FirebaseFirestore.getInstance()
                val audioData = hashMapOf(
                    "studentId" to studentId,
                    "pageId" to pageId,
                    "fileName" to "${studentId}-${pageId}",
                    "fileUrl" to uri.toString()
                )
                db.collection("quranRecordingStudents").add(audioData)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Upload successful", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Upload failed: $it", Toast.LENGTH_SHORT).show()
                    }
            }
        }.addOnFailureListener {
            Toast.makeText(context, "Upload failed: $it", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadAndSaveAudioById(documentId: String) {
        val file = selectedAudioUri!!
        val storageReference = FirebaseStorage.getInstance().reference.child("audio/${studentId}-${pageId}")
        val uploadTask = storageReference.putFile(file)

        uploadTask.addOnSuccessListener {
            storageReference.downloadUrl.addOnSuccessListener { uri ->
                val db = FirebaseFirestore.getInstance()
                val audioData = hashMapOf(
                    "fileName" to "${studentId}-${pageId}",
                    "fileUrl" to uri.toString()
                )
                db.collection("quranRecordingStudents").document(documentId)
                    .update(audioData as Map<String, String?>)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Upload successful", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Upload failed: $it", Toast.LENGTH_LONG).show()
                    }
            }
        }.addOnFailureListener {
            Toast.makeText(context, "Upload failed: $it", Toast.LENGTH_LONG).show()
        }
    }

    private fun getAudioRecord() {
        val db = FirebaseFirestore.getInstance()
        db.collection("quranRecordingStudents").whereEqualTo("pageId", pageId).whereEqualTo("studentId", studentId).get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.documents.isNotEmpty()) {
                    val document = querySnapshot.documents[0]
                    val documentId = document.id
                    uploadAndSaveAudioById(documentId)
                } else {
                    uploadAndSaveAudio()
                }
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
            }
    }
    private fun pickAudioFromStorage() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "audio/*"
        }
        startActivityForResult(intent, PICK_AUDIO_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_AUDIO_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.also { uri ->
                selectedAudioUri = uri
                binding.audioPlayerButtonPlay.visibility = View.VISIBLE
                binding.audioPlayerButtonPause.visibility = View.GONE
                mediaPlayer?.apply {
                    stop()
                    release()
                }
                mediaPlayer = null
                handler.removeCallbacks(updatePlayTimeThread)
                isPaused = false
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        handler.removeCallbacks(updatePlayTimeThread)
    }
}