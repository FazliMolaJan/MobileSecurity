package com.ssav.antitheft;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;

import com.ssav.R;

import java.io.IOException;
/**
 * Created by adnan on 7/8/13.
 */
public class AlarmService extends Service {
    private MediaPlayer mMediaPlayer;
    private final Handler mHandler = new Handler();

    public AlarmService() {
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent.hasExtra("stop")) {
            stopMediaPlayer();
            stopForeground(true);
            return START_NOT_STICKY;
        }
        alarmNotification(this);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        stopMediaPlayer();
        stopForeground(true);
    }

    private void stopMediaPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }


    @SuppressWarnings("deprecation")
    private void alarmNotification(Context context) {
        AudioManager am = (AudioManager) context
                .getSystemService(Context.AUDIO_SERVICE);


        Notification notification = new Notification(R.drawable.ic_launcher,
                "Sound override. Click to disable.",
                System.currentTimeMillis());

        boolean vibrate = true;

        int duration = 100;

        int maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
        am.setStreamVolume(AudioManager.STREAM_NOTIFICATION, maxVolume,
                AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);

        maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_RING);
        am.setStreamVolume(AudioManager.STREAM_RING, maxVolume,
                AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);

        Intent i = new Intent(context, AlarmService.class);
        i.putExtra("stop", true);

        PendingIntent pi = PendingIntent.getService(context, 0, i, 0);

        notification.setLatestEventInfo(context, "SSMS",
                "Sound override. Click to disable.", pi);;
        if (vibrate) {
            notification.vibrate = new long[] { 100, 200, 100, 500 };
        }

        AssetFileDescriptor afd = getResources().openRawResourceFd(R.raw.policesiren);

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
        mMediaPlayer.setLooping(true);
        try {
            mMediaPlayer.setDataSource(afd.getFileDescriptor(),
                    afd.getStartOffset(), afd.getLength());
            mMediaPlayer.prepare();
        } catch (IOException e) {
            stopForeground(true);
            mMediaPlayer.release();
            return;
        }
        mMediaPlayer.start();

        // Set duration time-out
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopMediaPlayer();
                stopForeground(true);
            }
        }, duration * 1000);

        notification.flags |= Notification.FLAG_NO_CLEAR;
        startForeground(1242, notification);
    }
}