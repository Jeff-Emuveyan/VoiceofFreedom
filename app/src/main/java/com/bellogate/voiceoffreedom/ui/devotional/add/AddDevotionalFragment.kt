package com.bellogate.voiceoffreedom.ui.devotional.add

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bellogate.voiceoffreedom.R
import com.bellogate.voiceoffreedom.data.devotional.SyncMultipleDevotionalsManager
import com.bellogate.voiceoffreedom.ui.SharedViewModel
import com.bellogate.voiceoffreedom.util.centerToast
import com.bellogate.voiceoffreedom.util.showAlert
import kotlinx.android.synthetic.main.add_devotional_fragment.*


class AddDevotionalFragment : Fragment() {

    private lateinit var viewModel: AddDevotionalViewModel
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var addDevotionalAdapter: AddDevotionalAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.add_devotional_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(AddDevotionalViewModel::class.java)
        sharedViewModel = ViewModelProviders.of(requireActivity()).get(SharedViewModel::class.java)

        sharedViewModel.showAddDevotionalFragment.value = false
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        displayDevotionalItemCollectors(requireContext(), 1)

        postButton.setOnClickListener{
            SyncMultipleDevotionalsManager.syncDevotionals(requireContext(), {
                centerToast("Uploading....")
                findNavController().popBackStack()
            }, {errorMessage ->
                showAlert("Oops", errorMessage)
            })
        }


        addButton.setOnClickListener {//add a new collector to the recyclerView
            if(viewModel.maximumNumberOfCollectorsReached(addDevotionalAdapter.numberOfCollectorsToShow)){
                centerToast("Maximum number reached")
                addButton.isEnabled = false
            }else{
                addDevotionalAdapter.numberOfCollectorsToShow += 1
                addDevotionalAdapter.notifyDataSetChanged()
            }


        }
    }


    private fun displayDevotionalItemCollectors(context: Context, numberOfCollectorsToShow: Int){
        recyclerView.layoutManager = LinearLayoutManager(context)
        addDevotionalAdapter = AddDevotionalAdapter(requireActivity(), numberOfCollectorsToShow)
        recyclerView.adapter = addDevotionalAdapter


    }
}
