package com.bellogate.voiceoffreedom.ui.devotional

import android.app.DatePickerDialog.OnDateSetListener
import android.graphics.Bitmap
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
import com.bellogate.voiceoffreedom.ui.SharedViewModel
import com.bellogate.voiceoffreedom.ui.devotional.util.UIState
import com.bellogate.voiceoffreedom.util.getSimpleDateFormat
import com.bellogate.voiceoffreedom.util.showDatePickerDialog
import com.bellogate.voiceoffreedom.util.todayDate
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

        date = todayDate(System.currentTimeMillis()).toString()

        this getDevotionalByDate date

        viewModel.devotional.observe(viewLifecycleOwner, Observer {
            setUpUIState(it.first, date, it.second?.bitmapUrlLink)
        })

        sharedViewModel.showManageDevotionalsFragment.observe(viewLifecycleOwner, Observer {
            if(it){
                findNavController().navigate(R.id.action_nav_devotional_to_manageDevotionalsFragment)
            }
        })

        //this will cause the MainActivity to call 'onCreateOptionsMenu' again.
        //If the user is an Admin, the MainActivity will add a menu item to 'Manage Devotionals'
        requireActivity().invalidateOptionsMenu()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imageHasSuccessfullyLoaded = false

        buttonRetry.setOnClickListener{
            this getDevotionalByDate date
        }

        buttonDate.setOnClickListener {
            showDatePickerDialog(requireContext(), this)
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

    private fun setUpUIState(uiState: UIState, date: String, imageUrl: String?){
        this.imageUrl = imageUrl
        buttonDate.text = date
        imageHasSuccessfullyLoaded = false

        when(uiState){
            UIState.LOADING -> {
                shimmer.showShimmer(true)
                shimmer.startShimmer()
                buttonRetry.visibility = View.INVISIBLE
                tvNoDataFoundForDate.visibility = View.INVISIBLE
            }
            UIState.FOUND -> {
                if(imageUrl != null){
                    Picasso.get().load(imageUrl).placeholder(R.drawable.dummy_devotional)
                        .error(R.drawable.ic_broken_image).into(imageView, object : Callback {
                            override fun onError(e: Exception?) {
                                shimmer.stopShimmer()
                                shimmer.hideShimmer()
                                imageView.setImageResource(R.drawable.ic_broken_image)
                                Toast.makeText(requireContext(), "Failed to load image", Toast.LENGTH_LONG).show()
                                imageHasSuccessfullyLoaded = false
                            }
                            override fun onSuccess() {
                                shimmer.stopShimmer()
                                shimmer.hideShimmer()
                                imageHasSuccessfullyLoaded = true
                            }
                        })
                }else{
                    imageView.setImageResource(R.drawable.dummy_devotional)
                }
                buttonRetry.visibility = View.INVISIBLE
                tvNoDataFoundForDate.visibility = View.INVISIBLE
            }
            UIState.FAILED_TO_LOAD ->{
                shimmer.stopShimmer()
                shimmer.hideShimmer()
                imageView.setImageResource(R.drawable.dummy_devotional)
                buttonRetry.visibility = View.VISIBLE
                Toast.makeText(requireContext(), "Failed, try again", Toast.LENGTH_LONG).show()
                tvNoDataFoundForDate.visibility = View.INVISIBLE
            }
            UIState.NO_DATA_FOR_SELECTED_DATE ->{
                shimmer.stopShimmer()
                shimmer.hideShimmer()
                imageView.setImageResource(R.drawable.dummy_devotional)
                buttonRetry.visibility = View.VISIBLE
                tvNoDataFoundForDate.visibility = View.VISIBLE
            }

        }
    }


    private infix fun getDevotionalByDate(dateToFind: String){
        viewModel.getDevotionalByDate(requireContext(),dateToFind)
        setUpUIState(UIState.LOADING, dateToFind, null)
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

}
