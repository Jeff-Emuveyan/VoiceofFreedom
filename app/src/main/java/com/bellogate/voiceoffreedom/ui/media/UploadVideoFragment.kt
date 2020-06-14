package com.bellogate.voiceoffreedom.ui.media

import android.app.ProgressDialog
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bellogate.voiceoffreedom.R
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.fragment_upload_video.*
import java.io.ByteArrayOutputStream
import java.util.*
import java.util.concurrent.TimeUnit


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [UploadVideoFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UploadVideoFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null


    private var filePath : Uri? = null
    private var thumbUri : Uri? = null
    private val PICK_IMAGE_RQUEST_CODE= 1234
    private var storage : FirebaseStorage? = null
    private var storageReference : StorageReference? = null
    lateinit var linkOfVideo: String
    lateinit var linkOfThumbnail: String
    lateinit var durationOfVideo: String
    lateinit var bitmapFile: Bitmap

    private var videoDuration : String? = null

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

        /**I use this section to initialize Firebase
         *
         */
        storage = FirebaseStorage.getInstance()
        storageReference = storage!!.reference


        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_upload_video, container, false)
    }


    /*** This is onViewCreated
     *
      */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttonSelectVideo.setOnClickListener(View.OnClickListener {
            showFileChooserVideo()
        })

        buttonUploadAll.setOnClickListener(View.OnClickListener {
            uploadVideo()
        })

    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment UploadVideoFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            UploadVideoFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }


    /**
     * This method will help
     * in choosing the
     * video file
     */
    private fun showFileChooserVideo(){
        val intent = Intent()
        intent.type = "video/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent,"SELECT video"), PICK_IMAGE_RQUEST_CODE)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            PICK_IMAGE_RQUEST_CODE -> if (resultCode === -1) {
                filePath = data!!.data
                var filePathXX = filePath?.path.toString()
                textViewDirectory.text = filePathXX.toString()

                Glide.with(this).load(filePath).into(imageViewVideoThumbnail)


                val mMMR = MediaMetadataRetriever()
                mMMR.setDataSource(context, filePath)
                var bitmap = mMMR.frameAtTime

                bitmapFile = bitmap


                val mp: MediaPlayer = MediaPlayer.create(this.context, Uri.parse(filePath.toString()))
                val duration = mp.duration
                videoDuration = duration.toString()
                mp.release()


                var dur = String.format("%d min, %d sec",
                    TimeUnit.MILLISECONDS.toMinutes(duration.toLong()),
                    TimeUnit.MILLISECONDS.toSeconds(duration.toLong()) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration.toLong()))
                )

                durationOfVideo = dur


                textViewDuration.text = dur.toString()

                val resources: Resources = resources

            }
        }
    }


    private fun uploadText(){

        var mTitle = editTextVideoTitle.text.toString()


        val db = FirebaseFirestore.getInstance()

        // Create a new user with a first, middle, and last name

        val video: HashMap<String, Any> = HashMap()
        video["title"] = mTitle
        video["url"] = linkOfVideo.toString()
        video["thumbnailUrl"] = linkOfThumbnail.toString()
        video["duration"] = durationOfVideo.toString()


        db.collection("videos")
            .document()
            .set(video)
            .addOnSuccessListener {
                Toast.makeText(this.context, "Details Uploaded successfully", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {
                Toast.makeText(this.context, "Failed", Toast.LENGTH_LONG).show() }
            .addOnCompleteListener {
                Toast.makeText(this.context, "Complete", Toast.LENGTH_LONG).show() }

    }


    //Upload Video file
    private fun uploadVideo() {
        val progressDialog = ProgressDialog(this.context)
        progressDialog.setTitle("Please wait...");
        progressDialog.show(); // will display the Progress Dialog.


        val details = editTextVideoTitle.text.toString()
        if (details.isEmpty()) {
            editTextVideoTitle.error = "Please enter some details."
            return
        }

        val videoRef = storageReference!!.child("videos/" + UUID.randomUUID().toString())
        videoRef.putFile(filePath!!)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this.context, "File Uploaded successfully", Toast.LENGTH_LONG).show()
                val resultXX = it.metadata!!.reference!!.downloadUrl;
                resultXX.addOnSuccessListener { it1 ->

                    val videoLink = it1.toString()
                    linkOfVideo = videoLink
                    Toast.makeText(this.context, videoLink, Toast.LENGTH_LONG).show()
                    //                    // Download directly from StorageReference using Glide
                    //                    // In case I want to Load image from the link to an Image view.
                    //                    Glide.with(this /* context */)
                    //                        .load(videoLink)
                    //                        .into(imageView)

                    uploadThumbnail()
                }
            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this.context, "Failed", Toast.LENGTH_LONG).show()
            }
            .addOnProgressListener { taskSnapShot ->
                val progress = 100.0 * taskSnapShot.bytesTransferred / taskSnapShot.totalByteCount
                progressDialog.setMessage("Video Uploaded " + progress.toInt() + "%...")
            }

    }




    //This is the function to upload Image file.
    private fun uploadThumbnail(){
        val progressDialog = ProgressDialog(this.context)
        progressDialog.setTitle("Please wait...");
        progressDialog.show(); // will display the Progress Dialog.


        val details = editTextVideoTitle.text.toString()
        if (details.isEmpty()){
            editTextVideoTitle.error = "Please enter some details."
            return
        }

        val imageRef = storageReference!!.child("thumbnails/"+ UUID.randomUUID().toString())

        val baos = ByteArrayOutputStream()
        bitmapFile.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data: ByteArray = baos.toByteArray()

        imageRef.putBytes(data)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this.context, "Thumbnail Uploaded successfully", Toast.LENGTH_LONG).show()
                val resultXX = it.metadata!!.reference!!.downloadUrl;
                resultXX.addOnSuccessListener { it1 ->

                    val imageLinkX = it1.toString()
                    linkOfThumbnail = imageLinkX
                    Toast.makeText(this.context, imageLinkX, Toast.LENGTH_LONG).show()
                    uploadText()

                }
            }
            .addOnFailureListener {e ->
                progressDialog.dismiss()
                Toast.makeText(this.context, "$e Failed", Toast.LENGTH_LONG).show()
            }
            .addOnProgressListener {taskSnapShot->
                val progress = 100.0 * taskSnapShot.bytesTransferred/taskSnapShot.totalByteCount
                progressDialog.setMessage("Thumbnail Uploaded " +progress.toInt() +"%...")
            }
    }




}