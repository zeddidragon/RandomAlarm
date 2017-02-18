package bear.panda.zeddy.randomalarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.BoolRes;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
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

import java.util.Calendar;

public class AlarmList extends AppCompatActivity {
    protected static Boolean[] activeDays =
            new Boolean[]{ true, true, true, true, true, false, false };
    private AlarmManager manager;
    private Intent soundTheAlarm;
    private PendingIntent pending;
    private int hour = 0;
    private int minute = 0;
    protected boolean isActive;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        soundTheAlarm = new Intent(AlarmList.this, AlarmReceiver.class);
        pending = PendingIntent.getBroadcast(AlarmList.this, 0, soundTheAlarm, 0);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        addDays();
        ToggleButton toggle = (ToggleButton) findViewById(R.id.toggleButton);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isActive = isChecked;
                setAlarm();
            }
        });
        TimePicker picker = (TimePicker) findViewById(R.id.timePicker);
        picker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minuteOfHour) {
                hour = hourOfDay;
                minute = minuteOfHour;
                setAlarm();
            }
        });
        if(new Build.VERSION.SDK_INT >= 23) {
            hour = picker.getHour();
            minute = picker.getMinute();
        } else {
            hour = picker.getCurrentHour();
            minute = picker.getCurrentMinute();
        }
    }

    public void setAlarm() {
        manager.cancel(pending);
        if(!isActive) {
            return;
        }
        Calendar c = Calendar.getInstance();
        long now = c.getTimeInMillis();

        c.set(Calendar.HOUR, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        long next = Long.MAX_VALUE;
        boolean isNext = false;

        for(int i = 0; i < activeDays.length; ++i) {
            if(!activeDays[i]) {
                continue;
            }

            // Calendar.DAY_OF_WEEK starts with Sunday, but my array starts with Monday
            // Furthermore, SUNDAY = 1, while my Monday = 0
            int day = (i + 1) % 7 + 1;

            c.set(Calendar.DAY_OF_WEEK, day);
            long time = c.getTimeInMillis();
            while(time <= now) {
                c.add(Calendar.WEEK_OF_YEAR, 1);
                time = c.getTimeInMillis();
            }
            c.add(Calendar.WEEK_OF_YEAR, -1);
            if(time < next) {
                next = time;
                isNext = true;
            }
        }

        if(isNext) {
            Log.i("Time to ring: ", String.format("%d", next));
            manager.setExact(AlarmManager.RTC_WAKEUP, next, pending);
        } else {
            ToggleButton toggle = (ToggleButton) findViewById(R.id.toggleButton);
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
            label.setText(day.getAbbr());
            label.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
            layout.setGravity(R.id.center);
            layout.setOrientation(1);  // Vertical
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
                    setAlarm();
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

    private class AlarmReceiver {
        public void handleMessage (Message msg){
            Bundle bundle = msg.getData();
            int hour = bundle.getInt("time_hour");
            int minute = bundle.getInt("time_minute");
            int day = bundle.getInt("time_day");
            Log.i("alarm", String.format("%d %d:%d", day, hour, minute));
            alarmOn();
        }
    }
}
