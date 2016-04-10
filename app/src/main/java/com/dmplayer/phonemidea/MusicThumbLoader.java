/*
 * This is the source code of DMPLayer for Android v. 1.0.0.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Copyright @Dibakar_Mistry, 2015.
 */
package com.dmplayer.phonemidea;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Stack;
import java.util.WeakHashMap;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.dmplayer.R;

public class MusicThumbLoader {
	private Map<ImageView, String> imageViews = Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
	private Map<String, Bitmap> videoBitmap = Collections.synchronizedMap(new WeakHashMap<String, Bitmap>());

	private Context context;

	public MusicThumbLoader(Context mContext) {
		// Make the background thead low priority. This way it will not affect
		// the UI performance
		this.context = mContext;
		photoLoaderThread.setPriority(Thread.NORM_PRIORITY - 1);
	}

	final int stub_id = R.drawable.bg_default_album_art;

	public void DisplayImage(String url, Context _context, ImageView imageView, ProgressBar progressBar) {
		try {
			imageViews.put(imageView, url);
			Bitmap bitmap = videoBitmap.get(url);

			if (bitmap != null) {
				imageView.setImageBitmap(bitmap);
				if (progressBar != null)
					progressBar.setVisibility(View.INVISIBLE);
			} else {
				queuePhoto(url, _context, imageView, progressBar);
				imageView.setImageResource(stub_id);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void queuePhoto(String url, Context _context, ImageView imageView, ProgressBar progressBar) {
		// This ImageView may be used for other images before. So there may be
		// some old tasks in the queue. We need to discard them.
		photosQueue.Clean(imageView);
		PhotoToLoad p = new PhotoToLoad(url, imageView, progressBar);
		synchronized (photosQueue.photosToLoad) {
			photosQueue.photosToLoad.push(p);
			photosQueue.photosToLoad.notifyAll();
		}

		// start thread if it's not started yet
		if (photoLoaderThread.getState() == Thread.State.NEW)
			photoLoaderThread.start();
	}

	private Bitmap getBitmap(String songid) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 1;
		Bitmap curThumb = null;
		try {
			Uri uri = Uri.parse("content://media/external/audio/media/" + songid + "/albumart");
			ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
			if (pfd != null) {
				FileDescriptor fd = pfd.getFileDescriptor();
				curThumb = BitmapFactory.decodeFileDescriptor(fd);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return curThumb;
	}

	// decodes image and scales it to reduce memory consumption
	private Bitmap decodeFile(File f) {
		try {
			// decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(new FileInputStream(f), null, o);

			// Find the correct scale value. It should be the power of 2.
			final int REQUIRED_SIZE = 80;
			int width_tmp = o.outWidth, height_tmp = o.outHeight;
			int scale = 1;
			while (true) {
				if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE)
					break;
				width_tmp /= 2;
				height_tmp /= 2;
				scale *= 2;
			}

			// decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
		} catch (FileNotFoundException e) {
		}
		return null;
	}

	// Task for the queue
	private class PhotoToLoad {
		public String url;
		public ImageView imageView;
		public ProgressBar progressBar;

		public PhotoToLoad(String u, ImageView i, ProgressBar p) {
			url = u;
			imageView = i;
			progressBar = p;
		}
	}

	PhotosQueue photosQueue = new PhotosQueue();

	public void stopThread() {
		photoLoaderThread.interrupt();
	}

	// stores list of photos to download
	class PhotosQueue {
		private Stack<PhotoToLoad> photosToLoad = new Stack<PhotoToLoad>();

		// removes all instances of this ImageView
		public void Clean(ImageView image) {
			for (int j = 0; j < photosToLoad.size();) {
				if (photosToLoad.get(j).imageView == image)
					photosToLoad.remove(j);
				else
					++j;
			}
		}
	}

	class PhotosLoader extends Thread {
		public void run() {
			try {
				while (true) {
					// thread waits until there are any images to load in the
					// queue
					if (photosQueue.photosToLoad.size() == 0)
						synchronized (photosQueue.photosToLoad) {
							photosQueue.photosToLoad.wait();
						}
					if (photosQueue.photosToLoad.size() != 0) {
						PhotoToLoad photoToLoad;
						synchronized (photosQueue.photosToLoad) {
							photoToLoad = photosQueue.photosToLoad.pop();
						}
						Bitmap bmp = getBitmap(photoToLoad.url);
						videoBitmap.put(photoToLoad.url, bmp);
						String tag = imageViews.get(photoToLoad.imageView);
						if (tag != null && tag.equals(photoToLoad.url)) {
							BitmapDisplayer bd = new BitmapDisplayer(bmp, photoToLoad.imageView, photoToLoad.progressBar);
							Activity a = (Activity) photoToLoad.imageView.getContext();
							a.runOnUiThread(bd);
						}
					}
					if (Thread.interrupted())
						break;
				}
			} catch (InterruptedException e) {
				// allow thread to exit
			}
		}
	}

	PhotosLoader photoLoaderThread = new PhotosLoader();

	// Used to display bitmap in the UI thread
	class BitmapDisplayer implements Runnable {
		Bitmap bitmap;
		ImageView imageView;
		ProgressBar progressBar;

		public BitmapDisplayer(Bitmap b, ImageView i, ProgressBar p) {
			bitmap = b;
			imageView = i;
			progressBar = p;
		}

		public void run() {
			if (bitmap != null)
				imageView.setImageBitmap(bitmap);
			else
				imageView.setImageResource(stub_id);

			if (progressBar != null)
				progressBar.setVisibility(View.INVISIBLE);

		}
	}

	public void clearCache() {

	}
}
