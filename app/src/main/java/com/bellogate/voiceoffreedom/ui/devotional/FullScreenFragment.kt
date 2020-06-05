package com.bellogate.voiceoffreedom.ui.devotional

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.bellogate.voiceoffreedom.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.devotional_fragment.*


class FullScreenFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_full_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val extras: Bundle? = requireArguments()
        var imageUrl = extras?.getString("imageUrl")!!

        if(!imageUrl.isNullOrEmpty()){
            Picasso.get().load(imageUrl).placeholder(R.drawable.dummy_devotional)
                .error(R.drawable.ic_broken_image).into(imageView)
        }

    }
}
