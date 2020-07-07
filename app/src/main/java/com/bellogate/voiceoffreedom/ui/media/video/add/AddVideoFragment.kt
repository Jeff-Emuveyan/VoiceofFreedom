package com.bellogate.voiceoffreedom.ui.media.video.add

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.bellogate.voiceoffreedom.R
import com.bellogate.voiceoffreedom.data.video.SyncVideoManager
import com.bellogate.voiceoffreedom.ui.SharedViewModel
import com.bellogate.voiceoffreedom.ui.devotional.util.AddVideoUIState
import com.bellogate.voiceoffreedom.util.Progress
import com.bellogate.voiceoffreedom.util.alertWithAction
import com.bellogate.voiceoffreedom.util.centerToast
import com.bellogate.voiceoffreedom.util.showAlert
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.add_video_fragment.*
import org.jetbrains.anko.support.v4.toast
import java.io.File


class AddVideoFragment : Fragment() {

    private lateinit var viewModel: AddVideoViewModel
    private lateinit var sharedViewModel: SharedViewModel
    private var videoSelected = false
    private var videoUri : Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.add_video_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(AddVideoViewModel::class.java)
        sharedViewModel = ViewModelProviders.of(requireActivity()).get(SharedViewModel::class.java)

        sharedViewModel.showAddVideoFragment.value = false

        viewModel.id.observe(viewLifecycleOwner, Observer {

            if(it != null){
                WorkManager.getInstance(requireContext())
                    // requestId is the WorkRequest id
                    .getWorkInfoByIdLiveData(it)
                    .observe(viewLifecycleOwner, Observer { workInfo: WorkInfo? ->
                        if (workInfo != null) {
                            val progress = workInfo.progress
                            val value = progress.getLong(Progress, 0)
                            // Do something with progress information
                            Log.e(SyncVideoManager::class.java.simpleName, "Recieving request: id: $it value: $value" )
                        }
                    })
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpUIState(AddVideoUIState.DEFAULT)
        videoSelected = false

        selectButton.setOnClickListener {
            openVideoGallery()
        }

        uploadButton.setOnClickListener {
            alertWithAction("Upload", "Upload this video?"){
                if(it){
                    uploadVideo()
                }
            }
        }
    }


    private fun openVideoGallery() {
        val intent = Intent()
        intent.type = "video/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, ""), 33)
    }


    private fun setUpUIState(addVideoUIState: AddVideoUIState){

        when(addVideoUIState){

            AddVideoUIState.DEFAULT -> {
                selectButton.isEnabled = true
                uploadButton.isEnabled = false
                buttonCancel.visibility = View.INVISIBLE
                progressBar.visibility = View.INVISIBLE
                tvTitle.visibility = View.INVISIBLE
                tvPercent.visibility = View.INVISIBLE
            }

            AddVideoUIState.VIDEO_SELECTED -> {
                selectButton.isEnabled = true
                uploadButton.isEnabled = true
                buttonCancel.visibility = View.INVISIBLE
                progressBar.visibility = View.INVISIBLE
                tvTitle.visibility = View.VISIBLE
                tvPercent.visibility = View.INVISIBLE
            }

            AddVideoUIState.VIDEO_UPLOADING -> {
                toast("Uploading...")
                selectButton.isEnabled = false
                uploadButton.isEnabled = false
                buttonCancel.visibility = View.VISIBLE
                progressBar.visibility = View.VISIBLE
                progressBar.isIndeterminate = true
                tvTitle.visibility = View.VISIBLE
                tvPercent.visibility = View.VISIBLE
                tvPercent.text = resources.getText(R.string.percent)
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == 33) {
            val selectedVideoUri: Uri? = data!!.data
            selectedVideoUri?.let {
                setUpUIState(AddVideoUIState.VIDEO_SELECTED)
                videoSelected = true
                videoUri = selectedVideoUri
                //use Glide to load the thumbnail into the imageView:
                Glide.with(requireContext()).load(it).into(imageViewThumbnail)
            }
        }else{
            toast("Cancelled")
        }
    }


    private fun uploadVideo(){
        if(videoUri != null){
            if(!tvTitle.text.isNullOrEmpty()) {
                setUpUIState(AddVideoUIState.VIDEO_UPLOADING)
                viewModel.uploadVideo(requireContext(), tvTitle.text.toString().trim(), videoUri!!)
            }else{
                centerToast("Please provide a title")
            }
        }else{
            centerToast("Select a video")
        }
    }
}
