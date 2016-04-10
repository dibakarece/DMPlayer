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
import android.graphics.drawable.BitmapDrawable;
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

public class ChildFragmentArtists extends Fragment {
    private static final String TAG = "ChildFragmentArtists";
    private static Context context;
    private RecyclerView recyclerView;
    boolean mIsUnknownArtist;
    boolean mIsUnknownAlbum;
    private ArtistsRecyclerAdapter mAdapter;

    public static ChildFragmentArtists newInstance(int position, Context mContext) {
        ChildFragmentArtists f = new ChildFragmentArtists();
        context = mContext;
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragmentchild_album, null);
        setupView(view);
        return view;
    }

    private void setupView(View v) {
        recyclerView = (RecyclerView) v.findViewById(R.id.recyclerview_grid);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        populateData();
    }

    private void populateData() {
        mAdapter = (ArtistsRecyclerAdapter) getActivity().getLastNonConfigurationInstance();
        if (mAdapter == null) {
            mAdapter = new ArtistsRecyclerAdapter(getActivity(), null);
            recyclerView.setAdapter(mAdapter);
            getArtistCursor(mAdapter.getQueryHandler(), null);
        } else {
            recyclerView.setAdapter(mAdapter);
            mArtistCursor = mAdapter.getCursor();
            if (mArtistCursor != null) {
                init(mArtistCursor);
            } else {
                getArtistCursor(mAdapter.getQueryHandler(), null);
            }
        }
    }

    public void resetView() {
        recyclerView.scrollToPosition(0);
    }

    private Cursor mArtistCursor;

    public void init(Cursor c) {
        if (mAdapter == null) {
            return;
        }
        mAdapter.changeCursor(c); // also sets mArtistCursor
        if (mArtistCursor == null) {
            DMPlayerUtility.displayDatabaseError(getActivity());
            mReScanHandler.sendEmptyMessageDelayed(0, 1000);
            return;
        }
    }

    private Handler mReScanHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (mAdapter != null) {
                getArtistCursor(mAdapter.getQueryHandler(), null);
            }
        }
    };

    private Cursor getArtistCursor(AsyncQueryHandler async, String filter) {

        String[] cols = new String[]{MediaStore.Audio.Artists._ID, MediaStore.Audio.Artists.ARTIST, MediaStore.Audio.Artists.NUMBER_OF_ALBUMS,
                MediaStore.Audio.Artists.NUMBER_OF_TRACKS};

        Uri uri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;
        if (!TextUtils.isEmpty(filter)) {
            uri = uri.buildUpon().appendQueryParameter("filter", Uri.encode(filter)).build();
        }

        Cursor ret = null;
        if (async != null) {
            async.startQuery(0, null, uri, cols, null, null, MediaStore.Audio.Artists.ARTIST_KEY);
        } else {
            ret = DMPlayerUtility.query(getActivity(), uri, cols, null, null, MediaStore.Audio.Artists.ARTIST_KEY);
        }
        return ret;
    }

    public class ArtistsRecyclerAdapter extends CursorRecyclerViewAdapter<ArtistsRecyclerAdapter.ViewHolder> {

        private final BitmapDrawable mDefaultAlbumIcon;
        private int mGroupArtistIdIdx;
        private int mGroupArtistIdx;
        private int mGroupAlbumIdx;
        private int mGroupSongIdx;
        private final Context mContext;
        private final Resources mResources;
        private final String mUnknownArtist;
        private final StringBuilder mBuffer = new StringBuilder();
        private MusicAlphabetIndexer mIndexer;
        private AsyncQueryHandler mQueryHandler;
        private String mConstraint = null;
        private boolean mConstraintIsValid = false;

        protected ArtistsRecyclerAdapter(Context context, Cursor cursor) {
            super(context, cursor);
            mQueryHandler = new QueryHandler(context.getContentResolver());

            Resources r = context.getResources();
            mDefaultAlbumIcon = (BitmapDrawable) r.getDrawable(R.drawable.bg_default_album_art);
            // no filter or dither, it's a lot faster and we can't tell the
            // difference
            mDefaultAlbumIcon.setFilterBitmap(false);
            mDefaultAlbumIcon.setDither(false);

            mContext = context;
            getColumnIndices(cursor);
            mResources = context.getResources();
            mUnknownArtist = context.getString(R.string.unknown_artist_name);
        }

        class QueryHandler extends AsyncQueryHandler {
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
            if (cursor != mArtistCursor) {
                mArtistCursor = cursor;
                getColumnIndices(cursor);
                super.changeCursor(cursor);
            }
        }

        public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
            String s = constraint.toString();
            if (mConstraintIsValid && ((s == null && mConstraint == null) || (s != null && s.equals(mConstraint)))) {
                return getCursor();
            }
            Cursor c = getArtistCursor(null, s);
            mConstraint = s;
            mConstraintIsValid = true;
            return c;
        }

        private void getColumnIndices(Cursor cursor) {
            if (cursor != null) {
                mGroupArtistIdIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists._ID);
                mGroupArtistIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST);
                mGroupAlbumIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS);
                mGroupSongIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_TRACKS);
                mGroupSongIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_TRACKS);
                if (mIndexer != null) {
                    mIndexer.setCursor(cursor);
                } else {
                    mIndexer = new MusicAlphabetIndexer(cursor, mGroupArtistIdx, mResources.getString(R.string.fast_scroll_alphabet));
                }
            }
        }

        protected class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView line1;
            TextView line2;
            ImageView icon;

            public ViewHolder(View itemView) {
                super(itemView);
                line1 = (TextView) itemView.findViewById(R.id.line1);
                line2 = (TextView) itemView.findViewById(R.id.line2);
                icon = (ImageView) itemView.findViewById(R.id.icon);
                icon.setScaleType(ScaleType.CENTER_CROP);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                try {
                    long artisID = getArtisID(getPosition());
                    Intent mIntent = new Intent(context, AlbumAndArtisDetailsActivity.class);
                    Bundle mBundle = new Bundle();
                    mBundle.putLong("id", artisID);
                    mBundle.putLong("tagfor", PhoneMediaControl.SonLoadFor.Artis.ordinal());
                    mBundle.putString("albumname", ((TextView) view.findViewById(R.id.line1)).getText().toString().trim());
                    mBundle.putString("title_one", "All my songs");
                    mBundle.putString("title_sec", ((TextView) view.findViewById(R.id.line2)).getText().toString().trim());
                    mIntent.putExtras(mBundle);
                    ((Activity) context).startActivity(mIntent);
                    ((Activity) context).overridePendingTransition(0, 0);
                } catch (Exception e) {
                    e.printStackTrace();
                    LogWriter.info(TAG, e.toString());
                }
            }
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, Cursor cursor) {

            String artist = cursor.getString(mGroupArtistIdx);
            String displayartist = artist;
            boolean unknown = artist == null || artist.equals(MediaStore.UNKNOWN_STRING);
            if (unknown) {
                displayartist = mUnknownArtist;
            }
            viewHolder.line1.setText(displayartist);

            int numalbums = cursor.getInt(mGroupAlbumIdx);
            int numsongs = cursor.getInt(mGroupSongIdx);

            String songs_albums = DMPlayerUtility.makeAlbumsLabel(context, numalbums, numsongs, unknown);

            viewHolder.line2.setText(songs_albums);

        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewgroup, int position) {
            return new ViewHolder(LayoutInflater.from(viewgroup.getContext()).inflate(R.layout.inflate_grid_item, viewgroup, false));
        }

        private long getArtisID(int position) {
            return getItemId(position);
        }
    }
}
