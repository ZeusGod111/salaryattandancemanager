package com.example.salaryattendancemanager

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.text.DecimalFormat
import java.util.Calendar

class MainActivity : AppCompatActivity() {
    private val df = DecimalFormat("#,##0.00")
    private var presentDays = 0
    private var markedToday = false
    private var reminderHour = 20
    private var reminderMinute = 0
    private lateinit var reminderStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        createNotificationChannel()

        val salary = findViewById<EditText>(R.id.etSalary)
        val days = findViewById<EditText>(R.id.etDays)
        val expected = findViewById<TextView>(R.id.tvExpected)
        val daily = findViewById<TextView>(R.id.tvDaily)
        val attendance = findViewById<TextView>(R.id.tvAttendance)
        val present = findViewById<Button>(R.id.btnPresent)
        val calcInput = findViewById<EditText>(R.id.etCalc)
        val calcButton = findViewById<Button>(R.id.btnCalc)
        val calcResult = findViewById<TextView>(R.id.tvCalcResult)

        val monthNames = arrayOf("জানুয়ারি","ফেব্রুয়ারি","মার্চ","এপ্রিল","মে","জুন","জুলাই","আগস্ট","সেপ্টেম্বর","অক্টোবর","নভেম্বর","ডিসেম্বর")
        val cal = Calendar.getInstance()
        findViewById<TextView>(R.id.tvMonth).text = "${monthNames[cal.get(Calendar.MONTH)]} ${cal.get(Calendar.YEAR)}"

        findViewById<Button>(R.id.btnCalculate).setOnClickListener {
            val s = salary.text.toString().toDoubleOrNull() ?: 0.0
            val d = days.text.toString().toDoubleOrNull() ?: 30.0
            val perDay = if (d > 0) s / d else 0.0
            expected.text = "৳ ${df.format(s)}"
            daily.text = "প্রতি দিনের মজুরি: ৳ ${df.format(perDay)}"
        }

        present.setOnClickListener {
            if (!markedToday) { presentDays++; markedToday = true; present.text = "আজকের হাজিরা — উপস্থিত ✓" }
            else { presentDays--; markedToday = false; present.text = "আজকের হাজিরা — উপস্থিত" }
            val total = days.text.toString().toIntOrNull() ?: 30
            val absent = (total - presentDays).coerceAtLeast(0)
            attendance.text = "উপস্থিত: $presentDays দিন   •   অনুপস্থিত: $absent দিন"
        }

        findViewById<Button>(R.id.btnOtCalculate).setOnClickListener {
            val hours = findViewById<EditText>(R.id.etOtHours).text.toString().toDoubleOrNull() ?: 0.0
            val rate = findViewById<EditText>(R.id.etOtRate).text.toString().toDoubleOrNull() ?: 0.0
            findViewById<TextView>(R.id.tvOtResult).text = "মোট OT: ৳ ${df.format(hours * rate)}"
        }

        calcButton.setOnClickListener { calcResult.text = calculateExpression(calcInput.text.toString()) }
        setupReminder()
    }

    private fun calculateExpression(input: String): String {
        val s = input.trim().replace("×", "*").replace("÷", "/")
        val m = Regex("^\\s*(-?\\d+(?:\\.\\d+)?)\\s*([+\\-*/])\\s*(-?\\d+(?:\\.\\d+)?)\\s*$").find(s)
            ?: return "সঠিক হিসাব লিখুন (যেমন 15000 / 26)"
        val a = m.groupValues[1].toDouble(); val op = m.groupValues[2]; val b = m.groupValues[3].toDouble()
        val r = when (op) { "+" -> a+b; "-" -> a-b; "*" -> a*b; "/" -> if (b != 0.0) a/b else return "শূন্য দিয়ে ভাগ করা যাবে না"; else -> return "ভুল হিসাব" }
        return "ফলাফল: ${df.format(r)}"
    }

    private fun setupReminder() {
        val text = findViewById<EditText>(R.id.etReminderText)
        val timeButton = findViewById<Button>(R.id.btnReminderTime)
        val setButton = findViewById<Button>(R.id.btnSetReminder)
        val cancelButton = findViewById<Button>(R.id.btnCancelReminder)
        reminderStatus = findViewById(R.id.tvReminderStatus)
        val prefs = getSharedPreferences("reminder", MODE_PRIVATE)
        reminderHour = prefs.getInt("hour", 20); reminderMinute = prefs.getInt("minute", 0)
        text.setText(prefs.getString("text", "হাজিরা দিতে ভুলবেন না"))
        updateTimeButton(timeButton)
        if (prefs.getBoolean("enabled", false)) reminderStatus.text = "রিমাইন্ডার চালু: ${formatTime(reminderHour, reminderMinute)}"

        timeButton.setOnClickListener {
            TimePickerDialog(this, { _, h, m -> reminderHour=h; reminderMinute=m; updateTimeButton(timeButton) }, reminderHour, reminderMinute, false).show()
        }
        setButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
            prefs.edit().putInt("hour", reminderHour).putInt("minute", reminderMinute).putString("text", text.text.toString().ifBlank { "হাজিরা দিতে ভুলবেন না" }).putBoolean("enabled", true).apply()
            scheduleReminder()
            reminderStatus.text = "রিমাইন্ডার চালু: ${formatTime(reminderHour, reminderMinute)}"
        }
        cancelButton.setOnClickListener { cancelReminder(); prefs.edit().putBoolean("enabled", false).apply(); reminderStatus.text = "রিমাইন্ডার বন্ধ" }
    }

    private fun updateTimeButton(button: Button) { button.text = "সময়: ${formatTime(reminderHour, reminderMinute)}" }
    private fun formatTime(h: Int, m: Int): String { val ap = if (h < 12) "AM" else "PM"; val hh = if (h % 12 == 0) 12 else h % 12; return String.format("%02d:%02d %s", hh, m, ap) }

    private fun scheduleReminder() {
        val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ReminderReceiver::class.java)
        val pi = PendingIntent.getBroadcast(this, 7001, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val now = Calendar.getInstance(); val next = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, reminderHour); set(Calendar.MINUTE, reminderMinute); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0); if (!after(now)) add(Calendar.DAY_OF_YEAR, 1) }
        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, next.timeInMillis, AlarmManager.INTERVAL_DAY, pi)
    }
    private fun cancelReminder() { val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager; val pi = PendingIntent.getBroadcast(this, 7001, Intent(this, ReminderReceiver::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE); am.cancel(pi) }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = android.app.NotificationChannel("salary_reminder", "বেতন ও হাজিরা রিমাইন্ডার", android.app.NotificationManager.IMPORTANCE_HIGH)
            channel.description = "কাস্টম রিমাইন্ডার নোটিফিকেশন"
            getSystemService(android.app.NotificationManager::class.java).createNotificationChannel(channel)
        }
    }
}
