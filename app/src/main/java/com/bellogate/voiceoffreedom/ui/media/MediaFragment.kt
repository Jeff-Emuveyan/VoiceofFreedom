package com.bellogate.voiceoffreedom.ui.media

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import co.paystack.android.PaystackSdk.applicationContext
import com.bellogate.voiceoffreedom.R
import com.bellogate.voiceoffreedom.ui.media.models.Post
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.firebase.ui.firestore.paging.LoadingState
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.fragment_media.*
import java.util.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MediaFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MediaFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    /***
     * this username will determine if floating button
     * is visible.
     */
    private var username: String? = "femi"

    private lateinit var mAdapter: FirestorePagingAdapter<Post, PostViewHolder>
    private val mFirestore = FirebaseFirestore.getInstance()
    private val mQuery = mFirestore.collection("videos").limit(4)





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_media, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerViewFB.layoutManager = LinearLayoutManager(this.context)

        // Init RecyclerView
        recyclerViewFB.setHasFixedSize(true)
        recyclerViewFB.layoutManager = LinearLayoutManager(this.context)

        setupAdapter()



        // Refresh Action on Swipe Refresh Layout
        swipeRefreshLayout.setOnRefreshListener {
            mAdapter.refresh()
        }




        /***
         * This the test that determines if username if "femi".
         * So if the username id not "femi", the floating button
         * is invisible.
         */
        if (username != "femi") {
            fltnButton.isVisible = false
        }

        fltnButton.setOnClickListener(View.OnClickListener {
            findNavController().navigate(R.id.uploadVideoFragment)
        })


    }

    private fun makeSnackBarMessage(message: String) {
        view?.let { Snackbar.make(it, message, Snackbar.LENGTH_SHORT).show() }
    }



    private fun setupAdapter() {

        // Init Paging Configuration
        val config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setInitialLoadSizeHint(1) //number of documents it will first fetch
            .setPageSize(3) //number of document it will add per refresh
            .build()

        // Init Adapter Configuration
        val options = FirestorePagingOptions.Builder<Post>()
            .setLifecycleOwner(this)
            .setQuery(mQuery, config, Post::class.java)
            .build()

        // Instantiate Paging Adapter
        mAdapter = object : FirestorePagingAdapter<Post, PostViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
                val view = layoutInflater.inflate(R.layout.video_list_row, parent, false)
                return PostViewHolder(view)
            }

            override fun onBindViewHolder(viewHolder: PostViewHolder, position: Int, post: Post) {
                // Bind to ViewHolder
                viewHolder.bind(post)
            }

            override fun onError(e: Exception) {
                super.onError(e)
                Log.e("MainActivity", e.message)
            }

            override fun onLoadingStateChanged(state: LoadingState) {
                when (state) {
                    LoadingState.LOADING_INITIAL -> {
                        swipeRefreshLayout.isRefreshing = true
                    }

                    LoadingState.LOADING_MORE -> {
                        swipeRefreshLayout.isRefreshing = true
                    }

                    LoadingState.LOADED -> {
                        swipeRefreshLayout.isRefreshing = false
                    }

                    LoadingState.ERROR -> {
                        Toast.makeText(
                            applicationContext,
                            "Error Occurred!",
                            Toast.LENGTH_SHORT
                        ).show()
                        swipeRefreshLayout.isRefreshing = false
                    }

                    LoadingState.FINISHED -> {
                        swipeRefreshLayout.isRefreshing = false
                    }
                }
            }

        }

        // Finally Set the Adapter to RecyclerView
        recyclerViewFB.adapter = mAdapter

    }

}

