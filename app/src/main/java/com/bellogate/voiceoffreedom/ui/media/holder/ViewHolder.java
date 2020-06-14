package com.bellogate.voiceoffreedom.ui.media.holder;

import android.app.Application;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bellogate.voiceoffreedom.R;
import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Log;

public class ViewHolder extends RecyclerView.ViewHolder {


    private SimpleExoPlayer exoPlayer;
    private PlayerView playerView;



    public ViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    public void setDetails2(final Application ctx, String title, final String url) {

        TextView mTitleTv2 = itemView.findViewById(R.id.rTitleTv2);
        playerView = itemView.findViewById(R.id.ep_video_view);

        TextView  titleFB = itemView.findViewById(R.id.textViewTitleFromFirebase);
        TextView  durationFB = itemView.findViewById(R.id.textViewDurationFromFirebase);
        ImageView imvFromFB = itemView.findViewById(R.id.imageViewThumbnailFromFirebase);

        mTitleTv2.setText(title);
        try {
            BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter.Builder(ctx).build();
            TrackSelector trackSelector = new DefaultTrackSelector(new AdaptiveTrackSelection.Factory(bandwidthMeter));
            exoPlayer = (SimpleExoPlayer) ExoPlayerFactory.newSimpleInstance(ctx);
            Uri video = Uri.parse(url);
            DefaultHttpDataSourceFactory dataSourceFactory = new DefaultHttpDataSourceFactory("video");
            ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
            MediaSource mediaSource = new ExtractorMediaSource(video, dataSourceFactory, extractorsFactory, null, null);
            playerView.setPlayer(exoPlayer);
//            exoPlayer.prepare(mediaSource); //buffer videos

            exoPlayer.setPlayWhenReady(false);
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT); //Aspect ratio

            exoPlayer.addListener(new Player.EventListener() {
                @Override
                public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
//                    Toast.makeText(ctx, "playWhenReady = " +playWhenReady + "   playbackState = "+ playbackState, Toast.LENGTH_LONG).show();
                    if (playWhenReady = true && playbackState == 1){
                        exoPlayer.prepare(mediaSource);

                    }
//                    Toast.makeText(ctx, Integer.toString(exoPlayer.getBufferedPercentage()), Toast.LENGTH_LONG).show();
                }
            });





//            playerView.setOnClickListener(new View.OnClickListener()
//            {
//                @Override
//                public void onClick(final View v)
//                {
//                    if (exoPlayer.getBufferedPosition() == 0.0){
//                        exoPlayer.prepare(mediaSource);
//
//                    }
//                }
//            });


        } catch (Exception e) {
            Log.e("ViewHolder2", "exoplayer error" + e.toString());
        }


    }

    public void setDetailsXX(final Application ctx, String title, final String thumbnailUrl, final String duration, final String url) {


        TextView titleFB = itemView.findViewById(R.id.textViewTitleFromFirebase);
        TextView durationFB = itemView.findViewById(R.id.textViewDurationFromFirebase);
        ImageView imvFromFB = itemView.findViewById(R.id.imageViewThumbnailFromFirebase);

        titleFB.setText(title);
        durationFB.setText(duration);
        Glide.with(ctx).load(thumbnailUrl).into(imvFromFB);


    }


}