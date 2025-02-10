package com.example.idmefv2alerter;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.PowerManager;
import android.os.SystemClock;

public class UpdateGPS extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK, "IDMEFv2 Alerter"
        );

        wakeLock.acquire(10 * 60 * 1000L); // 10 minutes

        executeTask(context);

        scheduleAlarm(context, 2 * 1000);

        wakeLock.release();
    }

    static public void scheduleAlarm(Context context, long trigTimer) {

        AlarmManager alarmManager = (AlarmManager) context.getSystemService (Context.ALARM_SERVICE);
        Intent intent = new Intent(context, UpdateGPS.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        long triggerTime = SystemClock.elapsedRealtime() + trigTimer;

        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    triggerTime,
                    pendingIntent
            );
        }
    }

    private void executeTask(Context context) {
        MainActivity mainActivity = MainActivity.getInstance();

        String myloc_text = null;
        LocationManager locationManager = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);
        try {
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                myloc_text = location.getLatitude() + ", " + location.getLongitude();
            } else {
                myloc_text = "Unknown location";
            }
        } catch (SecurityException e) {
            myloc_text = "You need to give location permission to fully use this app";
        }
        mainActivity.updateTextView(myloc_text, R.id.idmefv2_location);
    }
}