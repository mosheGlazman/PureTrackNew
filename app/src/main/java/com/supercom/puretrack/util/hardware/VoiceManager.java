package com.supercom.puretrack.util.hardware;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.Settings;

import com.supercom.puretrack.data.R;
import com.supercom.puretrack.data.source.local.table.TableEventConfig;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderStatusManager;
import com.supercom.puretrack.util.constants.OffenderActivation;

import java.util.concurrent.TimeUnit;

public class VoiceManager {
    private static final int PROXIMITY_AND_VIBRATE_TIME = 15;
    public static VoiceManager instance;

    public static VoiceManager getInstance(Context context) {
        if (instance == null) {
            instance = new VoiceManager(context);
        }
        return instance;
    }

    private VoiceManager(Context context) {
        this.context = context;
        init();
    }

    public void init() {
        mediaPlayer = MediaPlayer.create(context, R.raw.proximity_sound);
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        turnOffVolume(soundActionId);
    }

    Context context;
    private AudioManager audioManager;
    private MediaPlayer mediaPlayer;
    public Vibrator vibrator;
    int soundActionId;
    int valueMUSIC, valueALARM, valueVOICE_CALL, valueNOTIFICATION;

    public void runSoundAndVibrate(int eventAlramType) {
        boolean isOffenderAllocated = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(TableOffenderStatusManager.OFFENDER_STATUS_CONS.OFF_ACTIVATE_STATUS) == OffenderActivation.OFFENDER_STATUS_ALLOCATED;

        if (isOffenderAllocated) {
            long timeToStopSoundAndVibrate = 0;
            if (eventAlramType == TableEventConfig.EventsAlarmsType.DEVICE_SETTINGS) {
                timeToStopSoundAndVibrate = TimeUnit.SECONDS.toMillis(1);
                mediaPlayer = MediaPlayer.create(context, Settings.System.DEFAULT_NOTIFICATION_URI);
                mediaPlayer.setLooping(false);
            } else if (eventAlramType == TableEventConfig.EventsAlarmsType.TAG_PROXIMITY) {
                timeToStopSoundAndVibrate = TimeUnit.SECONDS.toMillis(PROXIMITY_AND_VIBRATE_TIME);
                mediaPlayer = MediaPlayer.create(context, R.raw.proximity_sound);
                mediaPlayer.setLooping(true);
            }

            final int volumeActionId = turnOnVolume();

            mediaPlayer.start();

            long[] pattern = {0, 1000, 100};
            vibrator.vibrate(pattern, 0);

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    mediaPlayer.stop();
                    vibrator.cancel();
                    turnOffVolume(volumeActionId);
                }
            }, timeToStopSoundAndVibrate);
        }
    }

    public void stop() {
        try {
            vibrator.cancel();
            mediaPlayer.stop();

            turnOffVolume(soundActionId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int turnOnVolume() {
        // valueMUSIC        = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        // valueALARM        = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
        // valueVOICE_CALL   = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
        // valueNOTIFICATION = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
        //Log.i("Bug2","turnOnVolume "+ ++soundActionId);

        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM), 0);
        audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), 0);
        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION), 0);
        audioManager.setStreamVolume(AudioManager.STREAM_DTMF, audioManager.getStreamMaxVolume(AudioManager.STREAM_DTMF), 0);
        audioManager.setStreamVolume(AudioManager.STREAM_ACCESSIBILITY, audioManager.getStreamMaxVolume(AudioManager.STREAM_ACCESSIBILITY), 0);
        audioManager.setStreamVolume(AudioManager.STREAM_RING, audioManager.getStreamMaxVolume(AudioManager.STREAM_RING), 0);
        audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM), 0);

        return soundActionId;
    }

    public void turnOffVolume(int actionId) {
        if (soundActionId != actionId) {
            //Log.i("Bug2",soundActionId+" -> turnOffVolume "+actionId);
            return;
        }
        //Log.i("Bug2","turnOffVolume "+soundActionId);

        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, 0, 0);
        audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, 0, 0);
        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 0, 0);
        audioManager.setStreamVolume(AudioManager.STREAM_DTMF, 0, 0);
        audioManager.setStreamVolume(AudioManager.STREAM_ACCESSIBILITY, 0, 0);
        audioManager.setStreamVolume(AudioManager.STREAM_RING, 0, 0);
        audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, 0, 0);
    }

    boolean isCallRinging;

    public enum e_files {
        CallReceive, Success, Error;

        public int rawResId() {
            switch (this) {
                case CallReceive:
                    return R.raw.over_the_horizon_s7;
                case Success:
                    return R.raw.success;
                case Error:
                    return R.raw.buzzer;
            }

            return 0;
        }

        public boolean looping() {
            switch (this) {
                case CallReceive:
                    return true;
                case Success:
                case Error:
                    return false;
            }

            return false;
        }
    }

    public void playWav(final e_files file) {
        //Log.i("Bug2","playCallRing");
        if (file == e_files.CallReceive) {
            if (isCallRinging) {
                return;
            }
            isCallRinging = true;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mediaPlayer = MediaPlayer.create(context, file.rawResId());
                mediaPlayer.setLooping(file.looping());
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        if (file == e_files.CallReceive) {
                            isCallRinging = false;
                        } else {
                            stopWav();
                        }
                    }
                });
                mediaPlayer.start();
            }
        }).start();

    }

    public void stopWav() {
        isCallRinging = false;
        mediaPlayer.stop();
    }
}
