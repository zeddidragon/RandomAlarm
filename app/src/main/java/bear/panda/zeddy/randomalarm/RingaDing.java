package bear.panda.zeddy.randomalarm;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;

import java.io.IOException;
import java.util.Random;

/**
 * Created by zeddy on 22/02/2017.
 */
public class RingaDing extends IntentService {
    static int userVolume;
    static Intent incompleteWakeful;
    static AudioManager aManager;
    static MediaPlayer mPlayer;

    public static void fulfill() {
        if(incompleteWakeful == null) return;
        aManager.setStreamVolume(AudioManager.STREAM_ALARM, userVolume, 1);
        mPlayer.stop();
        mPlayer.release();
        AlarmReceiver.completeWakefulIntent(incompleteWakeful);
        incompleteWakeful = null;
        aManager = null;
        mPlayer = null;
    }

    public RingaDing() {
        super("RingaDing");
    }

    @Override
    public void onHandleIntent(Intent wakefulIntent) {
        if(incompleteWakeful != null) {
            AlarmReceiver.completeWakefulIntent(wakefulIntent);
            return;
        }

        incompleteWakeful = wakefulIntent;
        Context context = getApplicationContext();
        int[] alarms = new int[]{
                R.raw.alarm0,
                R.raw.alarm1,
                R.raw.alarm2,
                R.raw.alarm3,
                R.raw.alarm4,
                R.raw.alarm5,
                R.raw.alarm6,
                R.raw.alarm7,
                R.raw.alarm8,
                R.raw.alarm9,
                R.raw.alarm10,
                R.raw.alarm11,
                R.raw.alarm12,
                R.raw.alarm13,
                R.raw.alarm14,
                R.raw.alarm15,
                R.raw.alarm16,
                R.raw.alarm17,
                R.raw.alarm18,
                R.raw.alarm19,
                R.raw.alarm20,
                R.raw.alarm21,
                R.raw.alarm22,
                R.raw.alarm23,
                R.raw.alarm24,
                R.raw.alarm25,
                R.raw.alarm26,
                R.raw.alarm27
        };
        Random rng = new Random();
        int tone = alarms[rng.nextInt(alarms.length)];
        Uri uri = Uri.parse("android.resource://bear.panda.zeddy.randomalarm/" + tone);

        mPlayer = new MediaPlayer();
        mPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
        mPlayer.setLooping(true);
        try {
            mPlayer.setDataSource(context, uri);
            mPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        aManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        userVolume = aManager.getStreamVolume(AudioManager.STREAM_ALARM);
        aManager.setStreamVolume(
                AudioManager.STREAM_ALARM,
                aManager.getStreamMaxVolume(AudioManager.STREAM_ALARM),
                1
        );

        Intent shutAlarm = new Intent(context, ShutAlarm.class);
        PendingIntent pendingShutAlarm = PendingIntent.getService(context, 0, shutAlarm, PendingIntent.FLAG_ONE_SHOT);

        NotificationManager nManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification n = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.notification_alarm)
                .setContentTitle("WAKE UP")
                .setContentText("It's time.")
                .setAutoCancel(false)
                .setContentIntent(pendingShutAlarm)
                .build();

        nManager.notify(1, n);
        mPlayer.start();
    }
}
