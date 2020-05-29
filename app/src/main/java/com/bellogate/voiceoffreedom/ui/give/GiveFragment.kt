package com.bellogate.voiceoffreedom.ui.give

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.bellogate.voiceoffreedom.R
import com.bellogate.voiceoffreedom.ui.SharedViewModel
import com.bellogate.voiceoffreedom.util.AMOUNT
import com.bellogate.voiceoffreedom.util.KEY
import kotlinx.android.synthetic.main.give_fragment.*


class GiveFragment : Fragment() {

    companion object {
        fun newInstance() = GiveFragment()
    }

    private lateinit var viewModel: GiveViewModel
    private lateinit var sharedViewModel: SharedViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.give_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(GiveViewModel::class.java)
        sharedViewModel = ViewModelProviders.of(requireActivity()).get(SharedViewModel::class.java)

        viewModel.getUser(requireContext(), 1).observe(viewLifecycleOwner, Observer {

            if(it != null){
                proceedButton.text = "Proceed"
                proceedButton.setOnClickListener{
                    progressBar.visibility = View.VISIBLE
                    proceedButton.visibility = View.INVISIBLE

                    fetchSecretKey(requireContext()) { success, key ->
                        if(success && !key.isNullOrEmpty()){
                            val bundle = Bundle()
                            bundle.putString(KEY, key)
                            bundle.putString(AMOUNT, editTextAmount.text.toString().trim())
                            findNavController().navigate(R.id.action_nav_give_to_processCardFragment, bundle)
                        }else{
                            progressBar.visibility = View.INVISIBLE
                            proceedButton.visibility = View.VISIBLE
                            Toast.makeText(requireContext(), resources.getText(R.string.network_error), Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }else{
                proceedButton.text = "Sign in / Sign up"
                proceedButton.setOnClickListener{
                    sharedViewModel.startSignInProcess.value = true
                }
            }
        })
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar.visibility = View.INVISIBLE

        editTextAmount.doOnTextChanged { text, start, count, after ->
            tvAmount.text = "${resources.getText(R.string.naira)}${text}"
            proceedButton.isEnabled = enableProceedButton(text)
        }
    }

    private fun enableProceedButton(text: CharSequence?): Boolean =
        (!text.isNullOrEmpty() && !text.startsWith("0"))


    private fun fetchSecretKey(context: Context, response:(success: Boolean, secretKey: String?)-> Unit){
        viewModel.fetchKey(context){success, key ->
            response.invoke(success, key)
        }
    }
}
