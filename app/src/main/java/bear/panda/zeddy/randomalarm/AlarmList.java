package bear.panda.zeddy.randomalarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.LinearLayout;
import android.widget.CheckBox;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ToggleButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AlarmList extends AppCompatActivity {
    protected static Boolean[] activeDays =
            new Boolean[]{ true, true, true, true, true, false, false };
    private static boolean settingsLoaded = false;
    private static int hour = 0;
    private static int minute = 0;
    private static boolean isActive;
    private static ToggleButton toggle;
    private static PendingIntent pending;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);


        toggle = (ToggleButton) findViewById(R.id.toggleButton);
        TimePicker picker = (TimePicker) findViewById(R.id.timePicker);

        loadSettings(getApplicationContext());

        if(Build.VERSION.SDK_INT >= 23) {
            picker.setHour(hour);
            picker.setMinute(minute);
        } else {
            picker.setCurrentHour(hour);
            picker.setCurrentMinute(minute);
        }
        toggle.setChecked(isActive);

        addDays();
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isActive = isChecked;
                saveSettings();
                setAlarm(getApplicationContext());
            }
        });
        picker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minuteOfHour) {
                hour = hourOfDay;
                minute = minuteOfHour;
                saveSettings();
                setAlarm(getApplicationContext());
            }
        });
    }

    private static void loadSettings(Context context) {
        if(settingsLoaded) return;
        settingsLoaded = true;
        Calendar c = Calendar.getInstance();
        SharedPreferences settings = context.getSharedPreferences("clock_settings", 0);
        decodeDays(settings.getInt("days", 0b0011111));
        hour = settings.getInt("hour", c.get(Calendar.HOUR_OF_DAY));
        minute = settings.getInt("minute", c.get(Calendar.MINUTE));
        isActive = settings.getBoolean("active", false);
    }

    private void saveSettings() {
        SharedPreferences settings = getSharedPreferences("clock_settings", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("days", encodeDays());
        editor.putInt("hour", hour);
        editor.putInt("minute", minute);
        editor.putBoolean("active", isActive);
        editor.commit();
    }

    private static void decodeDays(int active) {
        for(int i = 0; i < activeDays.length; ++i) {
            activeDays[i] = (active & (int) Math.pow(2, i)) > 0;
        }
    }

    private int encodeDays() {
        int ret = 0;
        for(int i = 0; i < activeDays.length; ++i) {
            if(activeDays[i]) {
                ret += (int) Math.pow(2, i);
            }
        }
        return ret;
    }

    public static void setAlarm(Context context) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if(pending == null) {
            Intent soundTheAlarm = new Intent(context, AlarmReceiver.class);
            pending = PendingIntent.getBroadcast(context, 0, soundTheAlarm, 0);
        } else {
            manager.cancel(pending);
        }
        RingaDing.fulfill();
        loadSettings(context);
        if(!isActive) {
            return;
        }
        long now = Calendar.getInstance().getTimeInMillis();
        long next = Long.MAX_VALUE;

        boolean isNext = false;
        for(int i = 0; i < activeDays.length; ++i) {
            if(!activeDays[i]) {
                continue;
            }

            // Calendar.DAY_OF_WEEK starts with Sunday, but my array starts with Monday
            // Furthermore, SUNDAY = 1, while my Monday = 0
            int day = (i + 1) % 7 + 1;

            Calendar c = Calendar.getInstance();
            // HOUR_OF_DAY is not to be confused with HOUR, which only operates with 12 hours
            c.set(Calendar.HOUR_OF_DAY, hour);
            c.set(Calendar.MINUTE, minute);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            c.set(Calendar.DAY_OF_WEEK, day);
            long time = c.getTimeInMillis();
            while(time <= now) {
                c.add(Calendar.WEEK_OF_YEAR, 1);
                time = c.getTimeInMillis();
            }
            if(time < next) {
                next = time;
                isNext = true;
            }
        }

        if(isNext) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Log.i("alarm at", format.format(next));
            manager.setExact(AlarmManager.RTC_WAKEUP, next, pending);
        } else if(toggle != null) {
            toggle.setChecked(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_alarm_list, menu);
        return true;
    }

    public void addDays() {
        LinearLayout days = (LinearLayout) findViewById(R.id.days);
        days.setGravity(Gravity.FILL_HORIZONTAL);
        days.setPadding(0, 16, 0, 16);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        );
        LinearLayout.LayoutParams boxParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        );
        for(int i = 0; i < Day.values().length; ++i) {
            Day day = Day.values()[i];
            LinearLayout layout = new LinearLayout(getApplicationContext());
            CheckBox box = new CheckBox(getApplicationContext());
            TextView label = new TextView(getApplicationContext());
            label.setTextColor(0xC0FFFFFF);
            label.setText(day.getAbbr());
            label.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
            layout.setGravity(R.id.center);
            layout.setOrientation(LinearLayout.VERTICAL);  // Vertical
            layout.addView(box);
            layout.addView(label);
            box.setLayoutParams(boxParams);
            layout.setLayoutParams(params);
            box.setScaleX(1.5f);
            box.setScaleY(1.5f);
            box.setChecked(activeDays[i]);
            days.addView(layout);
            final int index = i;
            box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    activeDays[index] = isChecked;
                    saveSettings();
                    setAlarm(getApplicationContext());
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
