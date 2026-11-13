package com.veltrix.study;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import java.util.Calendar;

public class ReminderReceiver extends BroadcastReceiver {
    private static final String PREFS = "study_reminders";
    private static final String CHANNEL = "study_reminders";

    public static void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(CHANNEL, "Study reminders", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("A gentle nudge when it is time to study.");
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    public static boolean[] readDays(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        boolean[] days = new boolean[7];
        for (int i = 0; i < 7; i++) days[i] = prefs.getBoolean("day_" + i, false);
        return days;
    }

    public static void saveAndSchedule(Context context, int hour, int minute, boolean[] days) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit();
        editor.putBoolean("enabled", true).putInt("hour", hour).putInt("minute", minute);
        for (int i = 0; i < 7; i++) editor.putBoolean("day_" + i, days[i]);
        editor.apply();
        scheduleNext(context);
    }

    public static void cancel(Context context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean("enabled", false).apply();
        AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarms != null) alarms.cancel(pendingIntent(context));
    }

    public static void scheduleNext(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (!prefs.getBoolean("enabled", false)) return;
        int hour = prefs.getInt("hour", 18);
        int minute = prefs.getInt("minute", 0);
        boolean[] days = readDays(context);
        Calendar now = Calendar.getInstance();
        Calendar next = Calendar.getInstance();
        next.set(Calendar.SECOND, 0);
        next.set(Calendar.MILLISECOND, 0);
        boolean found = false;
        for (int offset = 0; offset < 8; offset++) {
            next.setTimeInMillis(now.getTimeInMillis());
            next.add(Calendar.DAY_OF_YEAR, offset);
            next.set(Calendar.HOUR_OF_DAY, hour);
            next.set(Calendar.MINUTE, minute);
            int javaDay = next.get(Calendar.DAY_OF_WEEK);
            int mondayIndex = (javaDay + 5) % 7;
            if (days[mondayIndex] && next.getTimeInMillis() > now.getTimeInMillis()) { found = true; break; }
        }
        if (!found) return;
        AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarms != null) {
            if (Build.VERSION.SDK_INT >= 23) alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.getTimeInMillis(), pendingIntent(context));
            else alarms.set(AlarmManager.RTC_WAKEUP, next.getTimeInMillis(), pendingIntent(context));
        }
    }

    private static PendingIntent pendingIntent(Context context) {
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.setAction("com.veltrix.study.STUDY_REMINDER");
        return PendingIntent.getBroadcast(context, 701, intent, PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0));
    }

    @Override public void onReceive(Context context, Intent intent) {
        if (!context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean("enabled", false)) return;
        createChannel(context);
        Intent open = new Intent(context, MainActivity.class);
        PendingIntent content = PendingIntent.getActivity(context, 702, open, PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0));
        Notification.Builder builder = Build.VERSION.SDK_INT >= 26 ? new Notification.Builder(context, CHANNEL) : new Notification.Builder(context);
        builder.setSmallIcon(R.drawable.ic_veltrix_mark)
            .setContentTitle("A little time for your goals")
            .setContentText("Your study session is waiting. Open VELTRIX STUDY when you're ready.")
            .setContentIntent(content)
            .setAutoCancel(true)
            .setPriority(Notification.PRIORITY_DEFAULT);
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) manager.notify(701, builder.build());
        scheduleNext(context);
    }
}
