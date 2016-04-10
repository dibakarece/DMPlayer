/*
 * This is the source code of DMPLayer for Android v. 1.0.0.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Copyright @Dibakar_Mistry, 2015.
 */
package com.dmplayer.childfragment;

import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AlphabetIndexer;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import com.dmplayer.R;
import com.dmplayer.activities.AlbumAndArtisDetailsActivity;
import com.dmplayer.adapter.CursorRecyclerViewAdapter;
import com.dmplayer.utility.LogWriter;
import com.dmplayer.phonemidea.DMPlayerUtility;
import com.dmplayer.phonemidea.MusicAlphabetIndexer;
import com.dmplayer.phonemidea.PhoneMediaControl;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class ChildFragmentAlbum extends Fragment {

    private static final String TAG = "ChildFragmentAlbum";
    private static Context context;
    private RecyclerView recyclerView;
    boolean mIsUnknownArtist;
    boolean mIsUnknownAlbum;
    private AlbumRecyclerAdapter mAdapter;

    private Cursor mAlbumCursor;
    private String mArtistId;

    public static ChildFragmentAlbum newInstance(int position, Context mContext) {
        ChildFragmentAlbum f = new ChildFragmentAlbum();
        context = mContext;
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragmentchild_album, null);
        setupView(view, savedInstanceState);
        return view;
    }

    private void setupView(View v, Bundle icicle) {
        recyclerView = (RecyclerView) v.findViewById(R.id.recyclerview_grid);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        populateData(icicle);
    }

    public void resetView() {
        recyclerView.scrollToPosition(0);
    }

    private void populateData(Bundle icicle) {
        mAdapter = (AlbumRecyclerAdapter) getActivity().getLastNonConfigurationInstance();
        if (mAdapter == null) {
            mAdapter = new AlbumRecyclerAdapter(getActivity(), null);
            recyclerView.setAdapter(mAdapter);
            getAlbumCursor(mAdapter.getQueryHandler(), null);
        } else {
            recyclerView.setAdapter(mAdapter);
            mAlbumCursor = mAdapter.getCursor();
            if (mAlbumCursor != null) {
                init(mAlbumCursor);
            } else {
                getAlbumCursor(mAdapter.getQueryHandler(), null);
            }
        }
    }

    public void init(Cursor c) {
        if (mAdapter == null) {
            return;
        }
        mAdapter.changeCursor(c); // also sets mAlbumCursor
        if (mAlbumCursor == null) {
            DMPlayerUtility.displayDatabaseError(getActivity());
            mReScanHandler.sendEmptyMessageDelayed(0, 1000);
            return;
        }
    }

    private Handler mReScanHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (mAdapter != null) {
                getAlbumCursor(mAdapter.getQueryHandler(), null);
            }
        }
    };

    private Cursor getAlbumCursor(AsyncQueryHandler async, String filter) {
        String[] cols = new String[]{MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ARTIST, MediaStore.Audio.Albums.ALBUM,
                MediaStore.Audio.Albums.ALBUM_ART};

        Cursor ret = null;
        Uri uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
        if (!TextUtils.isEmpty(filter)) {
            uri = uri.buildUpon().appendQueryParameter("filter", Uri.encode(filter)).build();
        }
        if (async != null) {
            async.startQuery(0, null, uri, cols, null, null, MediaStore.Audio.Albums.DEFAULT_SORT_ORDER);
        } else {
            ret = DMPlayerUtility.query(getActivity(), uri, cols, null, null, MediaStore.Audio.Albums.DEFAULT_SORT_ORDER);
        }
        return ret;
    }

    public class AlbumRecyclerAdapter extends CursorRecyclerViewAdapter<AlbumRecyclerAdapter.ViewHolder> {

        private int mAlbumIdx;
        private int mArtistIdx;
        private final Resources mResources;
        private final String mUnknownAlbum;
        private final String mUnknownArtist;
        private AlphabetIndexer mIndexer;
        private AsyncQueryHandler mQueryHandler;
        private String mConstraint = null;
        private boolean mConstraintIsValid = false;

        private DisplayImageOptions options;
        private ImageLoader imageLoader = ImageLoader.getInstance();

        protected AlbumRecyclerAdapter(Context context, Cursor cursor) {
            super(context, cursor);
            this.mQueryHandler = new QueryHandler(context.getContentResolver());
            this.mUnknownAlbum = context.getString(R.string.unknown_album_name);
            this.mUnknownArtist = context.getString(R.string.unknown_artist_name);
            this.mResources = context.getResources();

            this.options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.bg_default_album_art)
                    .showImageForEmptyUri(R.drawable.bg_default_album_art).showImageOnFail(R.drawable.bg_default_album_art).cacheInMemory(true)
                    .cacheOnDisk(true).considerExifParams(true).bitmapConfig(Bitmap.Config.RGB_565).build();
            getColumnIndices(cursor);
        }

        private class QueryHandler extends AsyncQueryHandler {
            QueryHandler(ContentResolver res) {
                super(res);
            }

            @Override
            protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
                init(cursor);
            }
        }

        public AsyncQueryHandler getQueryHandler() {
            return mQueryHandler;
        }

        @Override
        public void changeCursor(Cursor cursor) {
            if (getActivity().isFinishing() && cursor != null) {
                cursor.close();
                cursor = null;
            }
            if (cursor != mAlbumCursor) {
                mAlbumCursor = cursor;
                getColumnIndices(cursor);
                super.changeCursor(cursor);
            }
        }

        public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
            String s = constraint.toString();
            if (mConstraintIsValid && ((s == null && mConstraint == null) || (s != null && s.equals(mConstraint)))) {
                return getCursor();
            }
            Cursor c = getAlbumCursor(null, s);
            mConstraint = s;
            mConstraintIsValid = true;
            return c;
        }

        private void getColumnIndices(Cursor cursor) {

            if (cursor != null) {
                mAlbumIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM);
                mArtistIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST);
                if (mIndexer != null) {
                    mIndexer.setCursor(cursor);
                } else {
                    mIndexer = new MusicAlphabetIndexer(cursor, mAlbumIdx, mResources.getString(R.string.fast_scroll_alphabet));
                }
            }

        }


        @Override
        public void onBindViewHolder(ViewHolder viewHolder, Cursor cursor) {

            String displayname = cursor.getString(mAlbumIdx);
            String artistname = cursor.getString(mArtistIdx);

            boolean unknown = displayname == null || displayname.equals(MediaStore.UNKNOWN_STRING);
            viewHolder.albumName.setText(unknown ? mUnknownAlbum : displayname);
            viewHolder.albumName.setTag(mAlbumIdx);

            unknown = (artistname == null || artistname.equals(MediaStore.UNKNOWN_STRING));
            viewHolder.artistName.setText(unknown ? mUnknownArtist : artistname);
            viewHolder.artistName.setTag(mArtistIdx);

            String contentURI = "content://media/external/audio/albumart/" + cursor.getLong(0);
            imageLoader.displayImage(contentURI, viewHolder.icon, options);

        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewgroup, int position) {
            return new ViewHolder(LayoutInflater.from(viewgroup.getContext()).inflate(R.layout.inflate_grid_item, viewgroup, false));
        }

        protected class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView albumName;
            TextView artistName;
            ImageView icon;

            public ViewHolder(View itemView) {
                super(itemView);
                albumName = (TextView) itemView.findViewById(R.id.line1);
                artistName = (TextView) itemView.findViewById(R.id.line2);
                icon = (ImageView) itemView.findViewById(R.id.icon);
                icon.setScaleType(ScaleType.CENTER_CROP);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                try {
                    long albumID = getAlbumID(getPosition());

                    Intent mIntent = new Intent(context, AlbumAndArtisDetailsActivity.class);
                    Bundle mBundle = new Bundle();
                    mBundle.putLong("id", albumID);
                    mBundle.putLong("tagfor", PhoneMediaControl.SonLoadFor.Album.ordinal());
                    mBundle.putString("albumname", ((TextView) view.findViewById(R.id.line1)).getText().toString().trim());
                    mBundle.putString("title_one", ((TextView) view.findViewById(R.id.line2)).getText().toString().trim());
                    mBundle.putString("title_sec", "");
                    mIntent.putExtras(mBundle);
                    ((Activity) context).startActivity(mIntent);
                    ((Activity) context).overridePendingTransition(0, 0);
                } catch (Exception e) {
                    e.printStackTrace();
                    LogWriter.info(TAG, e.toString());
                }
            }
        }

        private long getAlbumID(int position) {
            return getItemId(position);
        }

    }
}
