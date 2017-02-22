package bear.panda.zeddy.randomalarm;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;

/**
 * Created by zeddy on 22/02/2017.
 */
public class ShutAlarm extends IntentService {
    public ShutAlarm() {
        super("ShutAlarm");
    }

    @Override
    public void onHandleIntent(Intent intent) {
        RingaDing.fulfill();
        NotificationManagerCompat.from(this).cancel(1);
        AlarmList.setAlarm(getApplicationContext());
    }
}
