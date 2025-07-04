package com.example.location.presentation

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.location.LatLong
import com.example.location.R
import com.example.location.data.roomrepo.ItemOffsetDecoration
import com.example.location.databinding.ShowAllMarksFragmentBinding
import com.yandex.mapkit.geometry.Point
import com.yandex.runtime.image.ImageProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class ShowAllMarksFragment:Fragment() {
    private var _binding: ShowAllMarksFragmentBinding? = null
    val binding get() = _binding!!
    private val filesDir = ApplicationMapKit.applicationContext().filesDir
    private lateinit var photoFile: File
    private var photoUri: Uri? = null
    lateinit var panoramaPlaceFragment: PanoramaPlaceFragment
     lateinit var  fragPanoramaPlace:FrameLayout
    private val marksViewModel by viewModels<MarksViewModel>
    {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                MarksViewModel() as T
        }
    }
    private val showMarksListAdapter =
       ShowAllMarksAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ShowAllMarksFragmentBinding.inflate(inflater)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragPanoramaPlace = requireActivity().findViewById<FrameLayout>(R.id.small_navHostFragment)
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val mainFragment = MainFragment()
                parentFragmentManager.beginTransaction().replace(R.id.navHostFragment,mainFragment).commit()

                panoramaPlaceFragment =
                    PanoramaPlaceFragment.newInstance(
                        ApplicationMapKit.LocalHelp.latitudeActivity,
                        ApplicationMapKit.LocalHelp.longitudeActivity
                    )
                (activity as Transaction).navigateTo(panoramaPlaceFragment)
        fragPanoramaPlace.bringToFront()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            marksViewModel.getAllMarks()
            delay(300)
            binding.marksListRecycler.adapter = showMarksListAdapter
            showMarksListAdapter.onItemLook={fileName->showMark(fileName)}
            showMarksListAdapter.onItemDelete={id->deleteMark(id)}
            binding.marksListRecycler.layoutManager = GridLayoutManager(requireContext(), 2).apply {
                GridLayoutManager.VERTICAL
            }
            binding.marksListRecycler.setHasFixedSize(true)
            binding.marksListRecycler.addItemDecoration(ItemOffsetDecoration(requireContext()))
            marksViewModel.marks2.collect { marks ->
                if (marks?.size!=0&&marks!=null) {
                    showMarksListAdapter.submitList(marks)
                }
            }
        }
    }
    private fun showMark(fileMark:String) {
        val photoFileFragment = PhotoFileFragment()
        val args: Bundle = Bundle()
        val fileNameStr= fileMark
        args.putString("photo",fileNameStr)
        val pathPhoto = getPathPhoto(fileMark)
        args.putString("path", pathPhoto);
        photoFileFragment.setArguments(args);
        photoFileFragment.show(childFragmentManager, "photoMark")
    }
    fun deleteMark(id:Int){
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            marksViewModel.getAllMarks()
            marksViewModel.marks2.collect { marks ->
                if (marks?.size!=0&&marks!=null) {
                    marks.map { mark ->
                        if (mark.id == id) {

                            marksViewModel.deleteMark(id)
                            photoFile =
                                File(filesDir, mark.photoFileName)
                            photoUri = FileProvider.getUriForFile(
                                requireActivity(),
                                "com.example.location.fileprovider",
                                photoFile
                            )
                            delay(200)
                            requireActivity().contentResolver.delete(photoUri!!, null, null)

                        }
                    }
                    delay(300)
                    marksViewModel.getAllMarks()
                    marksViewModel.marks2.collect { marks ->
                        if (marks?.size!=0&&marks!=null) {
                            showMarksListAdapter.submitList(marks)
                            showMarksListAdapter.notifyDataSetChanged()
                        }
                    }
                }

            }
        }
    }
    fun getPathPhoto(fileName: String): String{
        photoFile = File(filesDir, fileName)
        return photoFile.path
    }
    companion object {
        fun newInstance( ):ShowAllMarksFragment {
            return ShowAllMarksFragment()
            }
        }
    }
