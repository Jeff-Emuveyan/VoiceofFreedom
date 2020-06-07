package com.bellogate.voiceoffreedom.ui.devotional.add

import android.content.Context
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.bellogate.voiceoffreedom.R
import com.bellogate.voiceoffreedom.ui.SharedViewModel
import kotlinx.android.synthetic.main.add_devotional_fragment.*

class AddDevotionalFragment : Fragment() {

    private lateinit var viewModel: AddDevotionalViewModel
    private lateinit var sharedViewModel: SharedViewModel

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

        displayDevotionalItemCollectors(requireContext(), 5)
    }



    private fun displayDevotionalItemCollectors(context: Context, numberOfCollectorsToShow: Int){
        recyclerView.layoutManager = LinearLayoutManager(context)
        val addDevotionalAdapter = AddDevotionalAdapter(requireContext(), numberOfCollectorsToShow)
        recyclerView.adapter = addDevotionalAdapter
    }
}
