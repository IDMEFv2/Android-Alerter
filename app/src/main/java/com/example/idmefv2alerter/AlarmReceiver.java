package com.example.idmefv2alerter;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {

    static public long lastTriggerTime = 0;

    @Override
    public void onReceive(Context context, Intent intent) {

        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK, "IDMEFv2 Alerter"
        );

        wakeLock.acquire(10 * 60 * 1000L); // 10 minutes

        executeTask(context);

        scheduleAlarm(context, AlarmReceiver.lastTriggerTime, AlarmReceiver.lastTriggerTime);

        wakeLock.release();
    }

    static public void stopAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService (Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }
    static public void scheduleAlarm(Context context, long schedulePeriod, long trigTimer) {

        AlarmReceiver.lastTriggerTime = schedulePeriod;
        AlarmManager alarmManager = (AlarmManager) context.getSystemService (Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + trigTimer,
                    pendingIntent
            );
        }
    }

    private void executeTask(Context context) {
        View v = LayoutInflater.from(context.getApplicationContext()).inflate(R.layout.activity_main, null);
        MainActivity mainActivity = MainActivity.getInstance();
        mainActivity.sendIDMEF(context, v);
    }
}