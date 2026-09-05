package com.example.salaryattendancemanager

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val prefs = context.getSharedPreferences("reminder", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("enabled", false)) return
        val text = prefs.getString("text", "হাজিরা দিতে ভুলবেন না") ?: "হাজিরা দিতে ভুলবেন না"
        val open = PendingIntent.getActivity(context, 7002, Intent(context, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(context, "salary_reminder")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("বেতন হিসাব")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(open)
            .build()
        NotificationManagerCompat.from(context).notify(7003, notification)
    }
}
