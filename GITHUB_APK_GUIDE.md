# GitHub দিয়ে APK বানানোর সহজ নিয়ম

1. GitHub-এ New repository তৈরি করুন।
2. এই ZIP extract করে ভেতরের সব file repository-তে upload করুন।
3. Commit changes দিন।
4. GitHub-এর **Actions** tab-এ যান।
5. **Build Android APK** workflow চালু হবে।
6. Build শেষ হলে workflow খুলে **Artifacts** থেকে `SalaryAttendanceManager-APK` download করুন।
7. ZIP খুললে `app-debug.apk` পাবেন।

প্রথমবার workflow তৈরি হতে কয়েক মিনিট লাগতে পারে।
