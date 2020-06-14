package com.bellogate.voiceoffreedom.ui.media

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bellogate.voiceoffreedom.R
import com.bellogate.voiceoffreedom.ui.media.models.Post
import com.squareup.picasso.Picasso

class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    //in your constructor add FragmentManager
    fun getUrlOfClickedVideo(): String? {
        return urlOfClickedVideo
    }

    private fun setUrlOfClickedVideo(urlOfClickedVideo: String?) {
        this.urlOfClickedVideo = urlOfClickedVideo
    }

    private var urlOfClickedVideo: String? = null


    private var tit: TextView = itemView.findViewById(R.id.textViewTitleFromFirebase)
    private var durrr: TextView = itemView.findViewById(R.id.textViewDurationFromFirebase)
    private var thumbn: ImageView = itemView.findViewById(R.id.imageViewThumbnailFromFirebase)
    private var imvClick: ImageView = itemView.findViewById(R.id.imageViewClickToPlay)


    fun bind(post: Post) {
        tit.text = post.title
        durrr.text = post.duration
        var urlOfVid = post.url
        var urlOfThumb = post.thumbnailUrl


        PicassoClient.downloadImage(itemView.context,urlOfThumb,thumbn)
        imvClick.setOnClickListener(View.OnClickListener {
            val urlOfVideo: String = urlOfVid.toString()
//            Toast.makeText(it.context, urlOfVideo, Toast.LENGTH_LONG).show()

            setUrlOfClickedVideo(urlOfVideo)

            val myIntent =
                Intent(it.context, VideoPlayActivity::class.java)
            myIntent.putExtra("urll", urlOfVideo)
            it.context.startActivity(myIntent)
        })
    }

    internal object PicassoClient {
        fun downloadImage(
            c: Context?,
            imageUrl: String?,
            img: ImageView?
        ) {
            if (imageUrl!!.isNotEmpty() && imageUrl != null) {
                Picasso.get().load(imageUrl).into(img)
            } else {
                Picasso.get().load(R.drawable.ic_donate_money).into(img)
            }
        }
    }
}

