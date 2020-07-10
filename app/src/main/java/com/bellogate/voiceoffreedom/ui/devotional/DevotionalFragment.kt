package com.bellogate.voiceoffreedom.ui.devotional

import android.app.DatePickerDialog.OnDateSetListener
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.bellogate.voiceoffreedom.R
import com.bellogate.voiceoffreedom.model.User
import com.bellogate.voiceoffreedom.ui.SharedViewModel
import com.bellogate.voiceoffreedom.ui.devotional.util.DevotionalUIState
import com.bellogate.voiceoffreedom.util.*
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.devotional_fragment.*
import java.lang.Exception
import java.util.*

class DevotionalFragment : Fragment(), OnDateSetListener {

    private lateinit var viewModel: DevotionalViewModel
    private lateinit var sharedViewModel: SharedViewModel
    private var date: String = ""
    private var imageUrl : String? = ""
    private var imageHasSuccessfullyLoaded = false
    private var user: User? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.devotional_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(DevotionalViewModel::class.java)
        sharedViewModel = ViewModelProviders.of(requireActivity()).get(SharedViewModel::class.java)

        viewModel.getUser(requireContext(), 1).observe(viewLifecycleOwner, Observer {
            user = it
            if(user != null && user!!.isAdmin){
                //this will cause the MainActivity to call 'onCreateOptionsMenu' again.
                //If the user is an Admin, the MainActivity will add a menu item to 'Add Devotional'
                sharedViewModel.topMenuController.value = Fragments.DEVOTIONAL
            }
        })

        viewModel.devotional.observe(viewLifecycleOwner, Observer {
            setUpUIState(it.first, date, it.second?.bitmapUrlLink)

            val devotional = it.second
            buttonDelete.setOnClickListener {
                alertWithAction("Delete", "Delete this devotional?") { confirmed ->
                    if (confirmed) {
                        setUpUIState(DevotionalUIState.DELETE_IN_PROGRESS, date, null)
                        viewModel.deleteDevotional(requireContext(), devotional)
                    }
                }
            }
        })

        viewModel.deleteDevotional.observe(viewLifecycleOwner, Observer {
            setUpUIState(DevotionalUIState.DELETE_COMPLETE, date, null)
            if(it){
                showAlert("Successful", "Delete successful")
                //refresh
                this getDevotionalByDate date
            }else{
                showAlert("Delete failed", "Delete failed, try again")
            }
        })


        sharedViewModel.showAddDevotionalFragment.observe(viewLifecycleOwner, Observer {
            if(it){
                findNavController().navigate(R.id.action_nav_devotional_to_addDevotionalFragment)
            }
        })

        //fetch today's devotional
        date = todayDate(System.currentTimeMillis()).toString()

        this getDevotionalByDate date
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imageHasSuccessfullyLoaded = false

        buttonRetry.setOnClickListener{
            this getDevotionalByDate date
        }

        buttonDate.setOnClickListener {
            showDatePickerDialog(requireContext(), true, this)
        }

        imageView.setOnClickListener {
            if(imageHasSuccessfullyLoaded) {
                val bundle = Bundle()
                bundle.putString("imageUrl", imageUrl)
                findNavController().navigate(R.id.action_nav_devotional_to_fullScreenFragment, bundle)
            }else{
                Toast.makeText(requireContext(), "No image to show", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setUpUIState(devotionalUiState: DevotionalUIState, date: String, imageUrl: String?){
        this.imageUrl = imageUrl
        buttonDate.text = date
        imageHasSuccessfullyLoaded = false

        when(devotionalUiState){
            DevotionalUIState.LOADING -> {
                shimmer.showShimmer(true)
                shimmer.startShimmer()
                buttonRetry.visibility = View.INVISIBLE
                tvNoDataFoundForDate.visibility = View.INVISIBLE
                buttonDelete.visibility = View.INVISIBLE
            }
            DevotionalUIState.FOUND -> {
                if(imageUrl != null){
                    Picasso.get().load(imageUrl).placeholder(R.drawable.dummy_devotional)
                        .error(R.drawable.ic_broken_image).into(imageView, object : Callback {
                            override fun onError(e: Exception?) {
                                shimmer?.stopShimmer()
                                shimmer?.hideShimmer()
                                imageView.setImageResource(R.drawable.ic_broken_image)
                                Toast.makeText(requireContext(), "Failed to load image", Toast.LENGTH_LONG).show()
                                imageHasSuccessfullyLoaded = false
                            }
                            override fun onSuccess() {
                                shimmer?.stopShimmer()
                                shimmer?.hideShimmer()
                                imageHasSuccessfullyLoaded = true
                            }
                        })
                }else{
                    imageView.setImageResource(R.drawable.dummy_devotional)
                }
                buttonRetry.visibility = View.INVISIBLE
                tvNoDataFoundForDate.visibility = View.INVISIBLE
                buttonDelete.visibility = View.VISIBLE
            }
            DevotionalUIState.FAILED_TO_LOAD ->{
                shimmer.stopShimmer()
                shimmer.hideShimmer()
                imageView.setImageResource(R.drawable.dummy_devotional)
                buttonRetry.visibility = View.VISIBLE
                Toast.makeText(requireContext(), "Failed, try again", Toast.LENGTH_LONG).show()
                tvNoDataFoundForDate.visibility = View.INVISIBLE
                buttonDelete.visibility = View.INVISIBLE
            }
            DevotionalUIState.NO_DATA_FOR_SELECTED_DATE ->{
                shimmer.stopShimmer()
                shimmer.hideShimmer()
                imageView.setImageResource(R.drawable.dummy_devotional)
                buttonRetry.visibility = View.VISIBLE
                tvNoDataFoundForDate.visibility = View.VISIBLE
                buttonDelete.visibility = View.INVISIBLE
            }

            DevotionalUIState.DELETE_IN_PROGRESS ->{
                progressBarDelete.visibility = View.VISIBLE
                buttonDelete.visibility = View.INVISIBLE
                buttonDate.isEnabled = false
                buttonRetry.isEnabled = false
            }

            DevotionalUIState.DELETE_COMPLETE ->{
                progressBarDelete.visibility = View.INVISIBLE
                buttonDelete.visibility = View.VISIBLE
                buttonDate.isEnabled = true
                buttonRetry.isEnabled = true
            }
        }

        //this will always hide the delete button if the user is not an admin
        if(user == null || !(user!!.isAdmin)) buttonDelete.visibility = View.INVISIBLE
    }


    private infix fun getDevotionalByDate(dateToFind: String){
        viewModel.getDevotionalByDate(requireContext(),dateToFind)
        setUpUIState(DevotionalUIState.LOADING, dateToFind, null)
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, dayOfMonth)

        val realMonth = month + 1
        val dateInSimpleFormat = "$year-$realMonth-$dayOfMonth"
        buttonDate.text = dateInSimpleFormat

        date = getSimpleDateFormat(calendar.timeInMillis, "dd-MMM-yyyy")
        this getDevotionalByDate date
    }



    override fun onPause() {
        super.onPause()
        //reset thr top menu by removinng the "Add devotional" item:
        sharedViewModel.topMenuController.value = null
    }
}
