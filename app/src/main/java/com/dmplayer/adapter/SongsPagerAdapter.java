package com.dmplayer.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.dmplayer.R;
import com.dmplayer.manager.MusicPreferance;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class SongsPagerAdapter extends PagerAdapter {

    private Context context;
    private LayoutInflater inflater;
    private DisplayImageOptions options;
    private ImageLoader imageLoader = ImageLoader.getInstance();
    private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();

    public SongsPagerAdapter(Context context_) {
        this.context = context_;
        this.inflater = LayoutInflater.from(context_);
        this.options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.bg_default_album_art)
                .showImageForEmptyUri(R.drawable.bg_default_album_art).showImageOnFail(R.drawable.bg_default_album_art).cacheInMemory(true)
                .cacheOnDisk(true).considerExifParams(true).bitmapConfig(Bitmap.Config.RGB_565).build();
    }

    @Override
    public int getCount() {
        if (MusicPreferance.playlist == null) {
            return 0;
        } else {
            return MusicPreferance.playlist.size();
        }
    }

    @Override
    public View instantiateItem(ViewGroup container, final int position) {
        View infV = inflater.inflate(R.layout.inflate_albumart_pager, container, false);
        ImageView artImage = (ImageView) infV.findViewById(R.id.image_songAlbumbg_mid);
        String contentURI = "content://media/external/audio/media/" + MusicPreferance.playlist.get(position).getId() + "/albumart";
        imageLoader.displayImage(contentURI, artImage, options, animateFirstListener);
        container.addView(infV);
        return infV;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    private static class AnimateFirstDisplayListener extends SimpleImageLoadingListener {

        static final List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            if (loadedImage != null) {
                ImageView imageView = (ImageView) view;
                boolean firstDisplay = !displayedImages.contains(imageUri);
                if (firstDisplay) {
                    FadeInBitmapDisplayer.animate(imageView, 1000);
                    displayedImages.add(imageUri);
                }
            }
        }

    }
}
