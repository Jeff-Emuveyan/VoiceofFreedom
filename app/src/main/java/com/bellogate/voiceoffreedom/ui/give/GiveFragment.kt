package com.bellogate.voiceoffreedom.ui.give

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.bellogate.voiceoffreedom.R
import kotlinx.android.synthetic.main.give_fragment.*


class GiveFragment : Fragment() {

    companion object {
        fun newInstance() = GiveFragment()
    }

    private lateinit var viewModel: GiveViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.give_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(GiveViewModel::class.java)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        editTextAmount.doOnTextChanged { text, start, count, after ->
            tvAmount.text = "${resources.getText(R.string.naira)}${text}"
            proceedButton.isEnabled = enableProceedButton(text)
        }

        proceedButton.setOnClickListener{

        }

    }

    private fun enableProceedButton(text: CharSequence?): Boolean =
        (!text.isNullOrEmpty() && !text.startsWith("0"))

}
