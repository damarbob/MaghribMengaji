package com.simsinfotekno.maghribmengaji.ui.home

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.MenuRes
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.transition.MaterialSharedAxis
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.simsinfotekno.maghribmengaji.LoginActivity
import com.simsinfotekno.maghribmengaji.MainActivity
import com.simsinfotekno.maghribmengaji.MainViewModel
import com.simsinfotekno.maghribmengaji.R
import com.simsinfotekno.maghribmengaji.databinding.FragmentHomeBinding
import com.simsinfotekno.maghribmengaji.enums.QuranItemStatus
import com.simsinfotekno.maghribmengaji.enums.UserDataEvent
import com.simsinfotekno.maghribmengaji.event.OnUserDataLoaded
import com.simsinfotekno.maghribmengaji.ui.adapter.VolumeAdapter
import com.simsinfotekno.maghribmengaji.usecase.GetQuranVolumeByStatus
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class HomeFragment : Fragment() {

    companion object {
        private val TAG = HomeFragment::class.java.simpleName
    }

    private var _binding: FragmentHomeBinding? = null

    private val mainViewModel: MainViewModel by viewModels()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    // Views
    private lateinit var volumeAdapter: VolumeAdapter

    // Use case
    private val getQuranVolumeByStatus = GetQuranVolumeByStatus()

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
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this)[HomeViewModel::class.java]

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        /* Views */
        binding.homeTextTitle.text = Firebase.auth.currentUser?.displayName
            ?: getString(R.string.app_name)

        // Volume adapter
        // Set volume dataset with status on progress
        volumeAdapter = VolumeAdapter(
            getQuranVolumeByStatus.invoke(QuranItemStatus.ON_PROGRESS),
            findNavController(),
            this
        )

        // Volume list
        val recyclerView: RecyclerView = binding.homeRecyclerViewVolume
        recyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = volumeAdapter

        binding.homeTextLastWritten.text = String.format(requireContext().getString(R.string.quran_page), MainActivity.student.lastPageId) // Last written

        // Progress indicator
        val allPagesCount = 604
//        val progressPercentage = MainActivity.student.finishedPageIds?.count()?.times(100)?.div(allPagesCount)
//        binding.homeTextPagePercentage.text = (progressPercentage).toString()
//        if (progressPercentage != null) {
//            binding.homeProgressIndicatorPagePercentage.progress = if (progressPercentage < 5) 5 else progressPercentage
//        }

        /* Listeners */
        binding.homeButtonMenu.setOnClickListener {
            showMenu(it, R.menu.menu_main)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        EventBus.getDefault().unregister(this)
    }

    //In the showMenu function from the previous example:
    @SuppressLint("RestrictedApi")
    private fun showMenu(v: View, @MenuRes menuRes: Int) {
        val popup = PopupMenu(requireContext(), v)
        popup.menuInflater.inflate(menuRes, popup.menu)

        if (popup.menu is MenuBuilder) {
            val menuBuilder = popup.menu as MenuBuilder
            menuBuilder.setOptionalIconsVisible(true)
            for (item in menuBuilder.visibleItems) {
                val ICON_MARGIN = 8
                val iconMarginPx =
                    TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, ICON_MARGIN.toFloat(), resources.displayMetrics
                    )
                        .toInt()
                if (item.icon != null) {
                    item.icon = InsetDrawable(item.icon, iconMarginPx, 0, iconMarginPx, 0)
                }
            }
        }
        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_profile -> findNavController().navigate(R.id.action_navigation_home_to_navigation_profile)
                R.id.menu_sign_out -> {
                    mainViewModel.logout()
                    navigateToLoginActivity()
                }
            }
            return@setOnMenuItemClickListener false
        }
        popup.show()
    }

    private fun navigateToLoginActivity() {
        val intent = Intent(activity, LoginActivity::class.java)
        startActivity(intent)
        activity?.finish() // Optional: Finish MainActivity to prevent the user from coming back to it
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun _105606032024(event: OnUserDataLoaded) {
        if (event.userDataEvent == UserDataEvent.PAGE) {
            volumeAdapter.dataSet = getQuranVolumeByStatus.invoke(QuranItemStatus.ON_PROGRESS)
            volumeAdapter.notifyDataSetChanged()
            Log.d(TAG, "Added dataset to ")
        }
    }
}