/*
 * This is the source code of DMPLayer for Android v. 1.0.0.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Copyright @Dibakar_Mistry, 2015.
 */
package com.dmplayer.manager;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.RemoteControlClient;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.dmplayer.ApplicationDMPlayer;
import com.dmplayer.R;
import com.dmplayer.activities.DMPlayerBaseActivity;
import com.dmplayer.models.SongDetail;
import com.dmplayer.phonemidea.DMPlayerUtility;

public class MusicPlayerService extends Service implements AudioManager.OnAudioFocusChangeListener, NotificationManager.NotificationCenterDelegate {

    public static final String NOTIFY_PREVIOUS = "musicplayer.previous";
    public static final String NOTIFY_CLOSE = "musicplayer.close";
    public static final String NOTIFY_PAUSE = "musicplayer.pause";
    public static final String NOTIFY_PLAY = "musicplayer.play";
    public static final String NOTIFY_NEXT = "musicplayer.next";

    private RemoteControlClient remoteControlClient;
    private AudioManager audioManager;
    private static boolean ignoreAudioFocus = false;
    private PhoneStateListener phoneStateListener;

    private static boolean supportBigNotifications = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    private static boolean supportLockScreenControls = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        NotificationManager.getInstance().addObserver(this, NotificationManager.audioProgressDidChanged);
        NotificationManager.getInstance().addObserver(this, NotificationManager.audioPlayStateChanged);
        try {
            phoneStateListener = new PhoneStateListener() {
                @Override
                public void onCallStateChanged(int state, String incomingNumber) {
                    if (state == TelephonyManager.CALL_STATE_RINGING) {
                        if (MediaController.getInstance().isPlayingAudio(MediaController.getInstance().getPlayingSongDetail())
                                && !MediaController.getInstance().isAudioPaused()) {
                            MediaController.getInstance().pauseAudio(MediaController.getInstance().getPlayingSongDetail());
                        }
                    } else if (state == TelephonyManager.CALL_STATE_IDLE) {

                    } else if (state == TelephonyManager.CALL_STATE_OFFHOOK) {

                    }
                    super.onCallStateChanged(state, incomingNumber);
                }
            };
            TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            if (mgr != null) {
                mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
            }
        } catch (Exception e) {
            Log.e("tmessages", e.toString());
        }
        super.onCreate();
    }

    @SuppressLint("NewApi")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            SongDetail messageObject = MediaController.getInstance().getPlayingSongDetail();
            if (messageObject == null) {
                DMPlayerUtility.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        stopSelf();
                    }
                });
                return START_STICKY;
            }
            if (supportLockScreenControls) {
                ComponentName remoteComponentName = new ComponentName(getApplicationContext(), MusicPlayerReceiver.class.getName());
                try {
                    if (remoteControlClient == null) {
                        audioManager.registerMediaButtonEventReceiver(remoteComponentName);
                        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
                        mediaButtonIntent.setComponent(remoteComponentName);
                        PendingIntent mediaPendingIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, 0);
                        remoteControlClient = new RemoteControlClient(mediaPendingIntent);
                        audioManager.registerRemoteControlClient(remoteControlClient);
                    }
                    remoteControlClient.setTransportControlFlags(RemoteControlClient.FLAG_KEY_MEDIA_PLAY | RemoteControlClient.FLAG_KEY_MEDIA_PAUSE
                            | RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE | RemoteControlClient.FLAG_KEY_MEDIA_STOP
                            | RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS | RemoteControlClient.FLAG_KEY_MEDIA_NEXT);
                } catch (Exception e) {
                    Log.e("tmessages", e.toString());
                }
            }
            createNotification(messageObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return START_STICKY;
    }

    @SuppressLint("NewApi")
    private void createNotification(SongDetail mSongDetail) {
        try {
            String songName = mSongDetail.getTitle();
            String authorName = mSongDetail.getArtist();
            SongDetail audioInfo = MediaController.getInstance().getPlayingSongDetail();

            RemoteViews simpleContentView = new RemoteViews(getApplicationContext().getPackageName(), R.layout.player_small_notification);
            RemoteViews expandedView = null;
            if (supportBigNotifications) {
                expandedView = new RemoteViews(getApplicationContext().getPackageName(), R.layout.player_big_notification);
            }

            Intent intent = new Intent(ApplicationDMPlayer.applicationContext, DMPlayerBaseActivity.class);
            intent.setAction("openplayer");
            intent.setFlags(32768);
            PendingIntent contentIntent = PendingIntent.getActivity(ApplicationDMPlayer.applicationContext, 0, intent, 0);

            Notification notification = new NotificationCompat.Builder(getApplicationContext()).setSmallIcon(R.drawable.player)
                    .setContentIntent(contentIntent).setContentTitle(songName).build();

            notification.contentView = simpleContentView;
            if (supportBigNotifications) {
                notification.bigContentView = expandedView;
            }

            setListeners(simpleContentView);
            if (supportBigNotifications) {
                setListeners(expandedView);
            }

            Bitmap albumArt = audioInfo != null ? audioInfo.getSmallCover(ApplicationDMPlayer.applicationContext) : null;

            if (albumArt != null) {
                notification.contentView.setImageViewBitmap(R.id.player_album_art, albumArt);
                if (supportBigNotifications) {
                    notification.bigContentView.setImageViewBitmap(R.id.player_album_art, albumArt);
                }
            } else {
                notification.contentView.setImageViewResource(R.id.player_album_art, R.drawable.bg_default_album_art);
                if (supportBigNotifications) {
                    notification.bigContentView.setImageViewResource(R.id.player_album_art, R.drawable.bg_default_album_art);
                }
            }
            notification.contentView.setViewVisibility(R.id.player_progress_bar, View.GONE);
            notification.contentView.setViewVisibility(R.id.player_next, View.VISIBLE);
            notification.contentView.setViewVisibility(R.id.player_previous, View.VISIBLE);
            if (supportBigNotifications) {
                notification.bigContentView.setViewVisibility(R.id.player_next, View.VISIBLE);
                notification.bigContentView.setViewVisibility(R.id.player_previous, View.VISIBLE);
                notification.bigContentView.setViewVisibility(R.id.player_progress_bar, View.GONE);
            }

            if (MediaController.getInstance().isAudioPaused()) {
                notification.contentView.setViewVisibility(R.id.player_pause, View.GONE);
                notification.contentView.setViewVisibility(R.id.player_play, View.VISIBLE);
                if (supportBigNotifications) {
                    notification.bigContentView.setViewVisibility(R.id.player_pause, View.GONE);
                    notification.bigContentView.setViewVisibility(R.id.player_play, View.VISIBLE);
                }
            } else {
                notification.contentView.setViewVisibility(R.id.player_pause, View.VISIBLE);
                notification.contentView.setViewVisibility(R.id.player_play, View.GONE);
                if (supportBigNotifications) {
                    notification.bigContentView.setViewVisibility(R.id.player_pause, View.VISIBLE);
                    notification.bigContentView.setViewVisibility(R.id.player_play, View.GONE);
                }
            }

            notification.contentView.setTextViewText(R.id.player_song_name, songName);
            notification.contentView.setTextViewText(R.id.player_author_name, authorName);
            if (supportBigNotifications) {
                notification.bigContentView.setTextViewText(R.id.player_song_name, songName);
                notification.bigContentView.setTextViewText(R.id.player_author_name, authorName);
            }
            notification.flags |= Notification.FLAG_ONGOING_EVENT;
            startForeground(5, notification);

            if (remoteControlClient != null) {
                RemoteControlClient.MetadataEditor metadataEditor = remoteControlClient.editMetadata(true);
                metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, authorName);
                metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_TITLE, songName);
                if (audioInfo != null && audioInfo.getCover(ApplicationDMPlayer.applicationContext) != null) {
                    metadataEditor.putBitmap(RemoteControlClient.MetadataEditor.BITMAP_KEY_ARTWORK,
                            audioInfo.getCover(ApplicationDMPlayer.applicationContext));
                }
                metadataEditor.apply();
                audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setListeners(RemoteViews view) {
        try {
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(NOTIFY_PREVIOUS),
                    PendingIntent.FLAG_UPDATE_CURRENT);
            view.setOnClickPendingIntent(R.id.player_previous, pendingIntent);
            pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(NOTIFY_CLOSE), PendingIntent.FLAG_UPDATE_CURRENT);
            view.setOnClickPendingIntent(R.id.player_close, pendingIntent);
            pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(NOTIFY_PAUSE), PendingIntent.FLAG_UPDATE_CURRENT);
            view.setOnClickPendingIntent(R.id.player_pause, pendingIntent);
            pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(NOTIFY_NEXT), PendingIntent.FLAG_UPDATE_CURRENT);
            view.setOnClickPendingIntent(R.id.player_next, pendingIntent);
            pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(NOTIFY_PLAY), PendingIntent.FLAG_UPDATE_CURRENT);
            view.setOnClickPendingIntent(R.id.player_play, pendingIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("NewApi")
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (remoteControlClient != null) {
            RemoteControlClient.MetadataEditor metadataEditor = remoteControlClient.editMetadata(true);
            metadataEditor.clear();
            metadataEditor.apply();
            audioManager.unregisterRemoteControlClient(remoteControlClient);
            audioManager.abandonAudioFocus(this);
        }
        try {
            TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            if (mgr != null) {
                mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
            }
        } catch (Exception e) {
            Log.e("tmessages", e.toString());
        }
        NotificationManager.getInstance().removeObserver(this, NotificationManager.audioProgressDidChanged);
        NotificationManager.getInstance().removeObserver(this, NotificationManager.audioPlayStateChanged);
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        if (ignoreAudioFocus) {
            ignoreAudioFocus = false;
            return;
        }
        if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
            if (MediaController.getInstance().isPlayingAudio(MediaController.getInstance().getPlayingSongDetail())
                    && !MediaController.getInstance().isAudioPaused()) {
                MediaController.getInstance().pauseAudio(MediaController.getInstance().getPlayingSongDetail());
            }
        } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
            // MediaController.getInstance().playAudio(MediaController.getInstance().getPlayingMessageObject());
        }
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationManager.audioPlayStateChanged) {
            SongDetail mSongDetail = MediaController.getInstance().getPlayingSongDetail();
            if (mSongDetail != null) {
                createNotification(mSongDetail);
            } else {
                stopSelf();
            }
        }
    }

    @Override
    public void newSongLoaded(Object... args) {

    }

    public static void setIgnoreAudioFocus() {
        ignoreAudioFocus = true;
    }
}
