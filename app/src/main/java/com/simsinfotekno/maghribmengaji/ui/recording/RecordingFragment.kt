package com.simsinfotekno.maghribmengaji.ui.recording

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.MaterialSharedAxis
import com.simsinfotekno.maghribmengaji.R
import com.simsinfotekno.maghribmengaji.RecordingActivity
import com.simsinfotekno.maghribmengaji.databinding.FragmentRecordingBinding
import com.simsinfotekno.maghribmengaji.ui.audioplayer.AudioPlayerFragment
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class RecordingFragment : Fragment() {

    companion object {
        fun newInstance() = RecordingFragment()
    }

    private var _binding: FragmentRecordingBinding? = null

    private val binding get() = _binding!!

    private val viewModel: RecordingViewModel by viewModels()

    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var isRecording = false
    private var isPaused = false

    private var recordingStartTime: Long = 0L
    private var pauseOffset: Long = 0L
    private val handler = Handler(Looper.getMainLooper())

    private val updateRecordingTimeRunnable = object : Runnable {
        override fun run() {
            if (isRecording) {
                val elapsedMillis = if (isPaused) {
                    pauseOffset
                } else {
                    SystemClock.elapsedRealtime() - recordingStartTime + pauseOffset
                }
                val elapsedSeconds = TimeUnit.MILLISECONDS.toSeconds(elapsedMillis)
                val minutes = elapsedSeconds / 60
                val seconds = elapsedSeconds % 60
                binding.recordingTextViewTime.text = String.format("%02d:%02d", minutes, seconds)
                handler.postDelayed(this, 1000)
            }
        }
    }

    private var pageId: Int? = -1

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
    ): View? {
        _binding = FragmentRecordingBinding.inflate(inflater, container, false)

        pageId = arguments?.getInt("pageId")

        binding.recordingButtonStop.visibility = View.GONE
        binding.recordingButtonPause.visibility = View.GONE
        binding.recordingButtonResume.visibility = View.GONE

        binding.recordingTextViewPage.text = getString(R.string.recite_page, pageId.toString())

//        if (checkPermission()) {
//            // Permissions are already granted
//        } else {
//            requestPermission()
//        }

        binding.recordingButtonStart.setOnClickListener { startRecording() }
        binding.recordingButtonStop.setOnClickListener { stopRecording() }
        binding.recordingButtonPause.setOnClickListener { pauseRecording() }
        binding.recordingButtonResume.setOnClickListener { resumeRecording() }
        binding.recordingButtonToPlayer.setOnClickListener {
            val bundle = Bundle().apply {
                putInt("pageId", pageId!!)
            }
            findNavController().navigate(R.id.action_recordingFragment_to_audioPlayerFragment, bundle)
        }

        return binding.root
    }

    private fun startRecording() {
        if (isRecording) {
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "Permissions are not granted", Toast.LENGTH_SHORT).show()
                return
            }
        } else {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "Permissions are not granted", Toast.LENGTH_SHORT).show()
                return
            }
        }

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                setOutputFormat(MediaRecorder.OutputFormat.OGG)
                setAudioEncodingBitRate(450000)
                setAudioEncoder(MediaRecorder.AudioEncoder.OPUS)
                outputFile = File(requireContext().externalCacheDir?.absolutePath, "audio record page $pageId.ogg")
            } else {
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncodingBitRate(96000)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                outputFile = File(requireContext().externalCacheDir?.absolutePath, "audio record page $pageId.3gp")
            }
            setOutputFile(outputFile?.absolutePath)
            try {
                prepare()
                start()
                recordingStartTime = SystemClock.elapsedRealtime()
                handler.postDelayed(updateRecordingTimeRunnable, 0)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        isRecording = true
        binding.recordingButtonStart.visibility = View.GONE
        binding.recordingButtonStop.visibility = View.VISIBLE
        binding.recordingButtonPause.visibility = View.VISIBLE
        binding.recordingButtonResume.visibility = View.GONE
    }

    private fun stopRecording() {
        if (!isRecording) {
            return
        }

        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
        isRecording = false
        isPaused = false

        handler.removeCallbacks(updateRecordingTimeRunnable)
        binding.recordingTextViewTime.text = "00:00"

        binding.recordingButtonStart.visibility = View.VISIBLE
        binding.recordingButtonStop.visibility = View.GONE
        binding.recordingButtonPause.visibility = View.GONE
        binding.recordingButtonResume.visibility = View.GONE
        pauseOffset = 0L
    }

    private fun pauseRecording() {
        mediaRecorder?.pause()
        isPaused = true
        pauseOffset += SystemClock.elapsedRealtime() - recordingStartTime
        handler.removeCallbacks(updateRecordingTimeRunnable)

        binding.recordingButtonStart.visibility = View.GONE
        binding.recordingButtonStop.visibility = View.VISIBLE
        binding.recordingButtonPause.visibility = View.GONE
        binding.recordingButtonResume.visibility = View.VISIBLE
    }

    private fun resumeRecording() {
        mediaRecorder?.resume()
        isPaused = false
        recordingStartTime = SystemClock.elapsedRealtime()
        handler.postDelayed(updateRecordingTimeRunnable, 0)

        binding.recordingButtonStart.visibility = View.GONE
        binding.recordingButtonStop.visibility = View.VISIBLE
        binding.recordingButtonPause.visibility = View.VISIBLE
        binding.recordingButtonResume.visibility = View.GONE
    }

    private fun checkPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO)
        val result1 = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            Toast.makeText(requireContext(),"Permission not granted", Toast.LENGTH_LONG).show()
        }
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_MEDIA_AUDIO), 1)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            1 -> if (grantResults.isNotEmpty()) {
                val recordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val readAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (recordAccepted && readAccepted) {
                    Toast.makeText(context, "Permission Granted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}