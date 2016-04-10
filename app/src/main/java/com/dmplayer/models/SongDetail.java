/*
 * This is the source code of DMPLayer for Android v. 1.0.0.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Copyright @Dibakar_Mistry, 2015.
 */
package com.dmplayer.models;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;

public class SongDetail {
	public int id;
	public int album_id;
	public String artist;
	public String title;
	public String display_name;
	public String duration;
	public String path;
	public float audioProgress = 0.0f;
	public int audioProgressSec = 0;

	public SongDetail(int _id, int aLBUM_ID, String _artist, String _title, String _path, String _display_name, String _duration) {
		this.id = _id;
		this.album_id = aLBUM_ID;
		this.artist = _artist;
		this.title = _title;
		this.path = _path;
		this.display_name = _display_name;
		this.duration = TextUtils.isEmpty(_duration) ? "0" : String.valueOf((Long.valueOf(_duration) / 1000));
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getAlbum_id() {
		return album_id;
	}

	public void setAlbum_id(int album_id) {
		this.album_id = album_id;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDisplay_name() {
		return display_name;
	}

	public void setDisplay_name(String display_name) {
		this.display_name = display_name;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Bitmap getSmallCover(Context context) {

		// ImageLoader.getInstance().getDiskCache().g
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 1;
		Bitmap curThumb = null;
		try {
			Uri uri = Uri.parse("content://media/external/audio/media/" + getId() + "/albumart");
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

	public Bitmap getCover(Context context) {

		// ImageLoader.getInstance().getDiskCache().g
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 1;
		Bitmap curThumb = null;
		try {
			Uri uri = Uri.parse("content://media/external/audio/media/" + getId() + "/albumart");
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
}
