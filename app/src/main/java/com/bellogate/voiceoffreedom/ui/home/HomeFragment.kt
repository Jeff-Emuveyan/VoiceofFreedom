package com.bellogate.voiceoffreedom.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bellogate.voiceoffreedom.R
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType
import com.smarteist.autoimageslider.SliderAnimations
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.devotional_fragment.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.imageView
import kotlinx.android.synthetic.main.fragment_home.shimmer
import java.lang.Exception

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //setup images for the slider:
        val listOfImages = arrayListOf(SliderItem("Worship with us", R.drawable.a),
            SliderItem("Worship with us", R.drawable.b),
            SliderItem("Worship with us", R.drawable.c),
            SliderItem("Worship with us", R.drawable.d))

        imageSlider.setSliderAdapter(SliderAdapter(context, listOfImages));
        imageSlider.startAutoCycle();
        imageSlider.setIndicatorAnimation(IndicatorAnimationType.WORM);
        imageSlider.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION)

        tvNetworkError.setOnClickListener {
            fetchLatestEvent()
        }
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        homeViewModel =
            ViewModelProviders.of(this).get(HomeViewModel::class.java)


        homeViewModel.event.observe(viewLifecycleOwner, Observer {

            when(it.first){
                NetworkState.FOUND ->{
                    //display the image:
                    val event = it.second
                    if(event != null) {
                        Picasso.get().load(event.imageUrl).placeholder(R.drawable.ic_insert_photo)
                            .error(R.drawable.ic_broken_image).into(imageView, object : Callback {
                                override fun onError(e: Exception?) {
                                    showRetry()
                                }
                                override fun onSuccess() {
                                    shimmer.stopShimmer()
                                    shimmer.hideShimmer()
                                }
                            })
                    }else{
                        showRetry()
                    }
                }
                NetworkState.ERROR ->{
                    showRetry()
                }
            }
        })

        fetchLatestEvent()
    }

    private fun fetchLatestEvent() {
        tvNetworkError.visibility = View.GONE
        imageView.setImageResource(R.drawable.ic_insert_photo)
        shimmer.showShimmer(true)
        shimmer.startShimmer()
        homeViewModel.fetchLatestEvent()
    }


    private fun showRetry(){
        shimmer.stopShimmer()
        shimmer.hideShimmer()
        tvNetworkError.visibility = View.VISIBLE
        imageView.setImageResource(R.drawable.ic_broken_image)
    }
}
