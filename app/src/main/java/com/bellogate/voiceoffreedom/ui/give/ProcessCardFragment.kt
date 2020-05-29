package com.bellogate.voiceoffreedom.ui.give

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import co.paystack.android.model.Card
import com.bellogate.voiceoffreedom.R
import com.bellogate.voiceoffreedom.ui.give.util.CardInputValidator
import com.bellogate.voiceoffreedom.ui.give.util.CardProcessState
import com.bellogate.voiceoffreedom.util.AMOUNT
import com.bellogate.voiceoffreedom.util.KEY
import com.bellogate.voiceoffreedom.util.showAlertForSuccessfulPayment
import kotlinx.android.synthetic.main.process_card_fragment.*


class ProcessCardFragment : Fragment() {

    companion object {
        fun newInstance() = ProcessCardFragment()
    }

    private lateinit var viewModel: ProcessCardViewModel
    private lateinit var key: String
    private lateinit var amount: String
    private val cardInputValidator = CardInputValidator()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.process_card_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(ProcessCardViewModel::class.java)

        viewModel.setUpPayStack(requireContext(), key)

        viewModel.getUser(requireContext(), 1).observe(viewLifecycleOwner, Observer {
            it?.let {
                tvEmail.setText(it.email)
                tvEmail.keyListener = null

                verifyButton.setOnClickListener { _ ->
                    progressBar.visibility = View.VISIBLE
                    verifyButton.visibility = View.INVISIBLE

                    validateCardNumber()
                    if(cardInputValidator.allInputFieldsValid){
                        viewModel.processCard(requireActivity(),
                            cardInputValidator.getCard(),
                            amount.toInt(),
                            it.email)
                    }else{
                        Toast.makeText(requireContext(), "Missing or Invalid input", Toast.LENGTH_LONG).show()
                        progressBar.visibility = View.INVISIBLE
                        verifyButton.visibility = View.VISIBLE
                    }
                }
            }
        })

        viewModel.cardProcessState.observe(viewLifecycleOwner, Observer {
            progressBar.visibility = View.INVISIBLE
            when(it){
                CardProcessState.SUCCESS ->  {
                    Toast.makeText(requireContext(), "Successful!", Toast.LENGTH_LONG).show()
                    showAlertForSuccessfulPayment()
                }
                CardProcessState.OTP_SENT ->  Toast.makeText(requireContext(), "Enter OTP", Toast.LENGTH_LONG).show()
                CardProcessState.FAILED ->  {
                    Toast.makeText(requireContext(), "Failed, try again", Toast.LENGTH_LONG).show()
                    verifyButton.visibility = View.VISIBLE
                }
                CardProcessState.INVALID_CARD -> {
                    Toast.makeText(requireContext(), "Invalid card", Toast.LENGTH_LONG).show()
                    verifyButton.visibility = View.VISIBLE
                }
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar.visibility = View.INVISIBLE

        //get key and amount
        val extras: Bundle = requireArguments()
        key = extras.getString(KEY)!!
        amount = extras.getString(AMOUNT)!!

        tvAmount.setText(amount)
        tvAmount.keyListener = null

        tvYear.doOnTextChanged { text, _, _, _ ->
            if (text.isNullOrEmpty() || text.length < 2){
                tvYear.error = "Year must be two digits"
                cardInputValidator.isYearValid = false
            }

            if (!text.isNullOrEmpty() && text.length == 2){
                cardInputValidator.isYearValid = true
                cardInputValidator.year = text.toString()
            }
        }

        tvMonth.doOnTextChanged { text, _, _, _ ->
            if (text.isNullOrEmpty() || text.length < 2){
                tvMonth.error = "Month must be two digits"
                cardInputValidator.isMonthValid = false
            }

            if (!text.isNullOrEmpty() && text.length == 2){
                cardInputValidator.isMonthValid = true
                cardInputValidator.month = text.toString()
            }
        }

        tvCvv.doOnTextChanged { text, _, _, _ ->
            if (text.isNullOrEmpty() || text.length < 3){
                tvCvv.error = "CVV must be three digits"
                cardInputValidator.isCvvValid = false
            }

            if (!text.isNullOrEmpty() && text.length == 3){
                cardInputValidator.isCvvValid = true
                cardInputValidator.cvv = text.toString()
            }
        }

        tvCardNumber.doAfterTextChanged { text ->
            validateCardNumber()
        }
    }


    private fun validateCardNumber(){
        if(tvCardNumber.text.isNullOrEmpty()){
            cardInputValidator.isCardNumberValid = false
            tvCardNumber.error = "Invalid card number"
        }else{
            val card = Card(tvCardNumber.text.toString(), 0,0, "")
            if(!card.validNumber()){
                cardInputValidator.isCardNumberValid = false
                tvCardNumber.error = "Invalid card number"
            }else {
                cardInputValidator.isCardNumberValid = true
                cardInputValidator.cardNumber = tvCardNumber.text.toString()
            }
        }
    }
}
