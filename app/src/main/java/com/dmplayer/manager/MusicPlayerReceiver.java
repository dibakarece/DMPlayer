/*
 * This is the source code of DMPLayer for Android v. 1.0.0.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Copyright @Dibakar_Mistry, 2015.
 */
package com.dmplayer.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

public class MusicPlayerReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_MEDIA_BUTTON)) {
			if (intent.getExtras() == null) {
				return;
			}
			KeyEvent keyEvent = (KeyEvent) intent.getExtras().get(Intent.EXTRA_KEY_EVENT);
			if (keyEvent == null) {
				return;
			}
			if (keyEvent.getAction() != KeyEvent.ACTION_DOWN)
				return;

			switch (keyEvent.getKeyCode()) {
			case KeyEvent.KEYCODE_HEADSETHOOK:
			case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
				if (MediaController.getInstance().isAudioPaused()) {
					MediaController.getInstance().playAudio(MediaController.getInstance().getPlayingSongDetail());
				} else {
					MediaController.getInstance().pauseAudio(MediaController.getInstance().getPlayingSongDetail());
				}
				break;
			case KeyEvent.KEYCODE_MEDIA_PLAY:
				MediaController.getInstance().playAudio(MediaController.getInstance().getPlayingSongDetail());
				break;
			case KeyEvent.KEYCODE_MEDIA_PAUSE:
				MediaController.getInstance().pauseAudio(MediaController.getInstance().getPlayingSongDetail());
				break;
			case KeyEvent.KEYCODE_MEDIA_STOP:
				break;
			case KeyEvent.KEYCODE_MEDIA_NEXT:
				MediaController.getInstance().playNextSong();
				break;
			case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
				MediaController.getInstance().playPreviousSong();
				break;
			}
		} else {
			if (intent.getAction().equals(MusicPlayerService.NOTIFY_PLAY)) {
				MediaController.getInstance().playAudio(MediaController.getInstance().getPlayingSongDetail());
			} else if (intent.getAction().equals(MusicPlayerService.NOTIFY_PAUSE)
					|| intent.getAction().equals(android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
				MediaController.getInstance().pauseAudio(MediaController.getInstance().getPlayingSongDetail());
			} else if (intent.getAction().equals(MusicPlayerService.NOTIFY_NEXT)) {
				MediaController.getInstance().playNextSong();
			} else if (intent.getAction().equals(MusicPlayerService.NOTIFY_CLOSE)) {
				MediaController.getInstance().cleanupPlayer(context,true, true);
			} else if (intent.getAction().equals(MusicPlayerService.NOTIFY_PREVIOUS)) {
				MediaController.getInstance().playPreviousSong();
			}
		}
	}
}
