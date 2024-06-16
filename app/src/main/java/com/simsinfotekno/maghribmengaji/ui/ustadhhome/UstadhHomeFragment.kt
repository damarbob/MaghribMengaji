package com.simsinfotekno.maghribmengaji.ui.ustadhhome

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.transition.MaterialSharedAxis
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.simsinfotekno.maghribmengaji.LoginActivity
import com.simsinfotekno.maghribmengaji.MainViewModel
import com.simsinfotekno.maghribmengaji.R
import com.simsinfotekno.maghribmengaji.databinding.FragmentUstadhHomeBinding
import com.simsinfotekno.maghribmengaji.enums.UserDataEvent
import com.simsinfotekno.maghribmengaji.event.OnUserDataLoaded
import com.simsinfotekno.maghribmengaji.ui.adapter.StudentAdapter
import com.simsinfotekno.maghribmengaji.usecase.ShowPopupMenu
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class UstadhHomeFragment : Fragment() {

    private lateinit var binding: FragmentUstadhHomeBinding

    companion object {
        private val TAG = UstadhHomeFragment::class.java.simpleName
        fun newInstance() = UstadhHomeFragment()
    }

    /* View Model */
    private val viewModel: UstadhHomeViewModel by viewModels()
    private val mainViewModel: MainViewModel by viewModels()

    /* Views */
    private lateinit var studentAdapter: StudentAdapter

    /* Use cases */
    private val showPopupMenu: ShowPopupMenu = ShowPopupMenu()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        EventBus.getDefault().register(this)

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
        binding = FragmentUstadhHomeBinding.inflate(layoutInflater, container, false)

        Log.d(TAG, "Entering ustadh home fragment")

        /* Views */
        studentAdapter = StudentAdapter(listOf())

        val recyclerView = binding.ustadhHomeRecyclerViewStudent
        recyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = studentAdapter

        /* Observers */
        studentAdapter.selectedStudent.observe(viewLifecycleOwner) {
            if (it == null) return@observe

            Log.d(TAG, "studentAdapter.selectedStudent.observe: Student: ${it.id}")

            // Navigate to student volume list and pass student id
            val bundle = Bundle()
            bundle.putString("studentId", it.id)
            findNavController().navigate(R.id.action_ustadhHomeFragment_to_studentVolumeListFragment, bundle)
        }
        viewModel.getStudentResult.observe(viewLifecycleOwner, Observer { result ->
            result?.onSuccess {

                studentAdapter.dataSet = it
                studentAdapter.notifyDataSetChanged()

            }?.onFailure { exception ->
                // Show error message
                Toast.makeText(requireContext(), exception.message, Toast.LENGTH_SHORT).show()
            }
        })

        /* Listeners */
        binding.ustadhHomeButtonMenu.setOnClickListener {
            showPopupMenu(requireContext(), it, R.menu.menu_main) {
                when (it.itemId) {
                    R.id.menu_profile -> findNavController().navigate(R.id.action_ustadhHomeFragment_to_profileFragment)
                    R.id.menu_sign_out -> {
                        mainViewModel.logout()
                        navigateToLoginActivity()
                    }
                }
                return@showPopupMenu false
            }
        }

        /* Get ustadh list */
        getMyStudents()

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()

        EventBus.getDefault().unregister(this)
    }

    private fun navigateToLoginActivity() {
        val intent = Intent(activity, LoginActivity::class.java)
        startActivity(intent)
        activity?.finish() // Optional: Finish MainActivity to prevent the user from coming back to it
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun _105606032024(event: OnUserDataLoaded) {
        if (event.userDataEvent == UserDataEvent.PAGE) {
            getMyStudents()
        }
    }

    private fun getMyStudents() {
        if (studentAdapter.dataSet.size == 0) {
            Firebase.auth.currentUser?.let { viewModel.getStudentFromDb(it.uid) }
        }
    }

}