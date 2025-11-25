package com.example.myapp

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.widget.*

class MainActivity : Activity() {

    // رابط موقعك
    val myWebsiteUrl = "https://mhamed01023165311-del.github.io/Falcon.1/"
    
    // متغير لمعرفة حالة الرادار
    var isRadarOn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // التصميم
        val scrollView = ScrollView(this)
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.gravity = Gravity.CENTER
        layout.setPadding(40, 40, 40, 40)
        layout.setBackgroundColor(Color.parseColor("#F5F7FA"))

        // دوال مساعدة للتصميم
        fun createInput(hint: String): EditText {
            val input = EditText(this)
            input.hint = hint
            input.setBackgroundColor(Color.WHITE)
            input.setPadding(30, 30, 30, 30)
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            params.setMargins(0, 15, 0, 15)
            input.layoutParams = params
            return input
        }

        // 1. الخانات
        val imgLinkInput = createInput("رابط صورتك الشخصية (Link)")
        val nameInput = createInput("الاسم")
        val jobInput = createInput("الوظيفة")
        val phoneInput = createInput("رقم الهاتف")
        val addressInput = createInput("العنوان")
        val fbInput = createInput("رابط فيسبوك")

        // 2. زر الرادار (تشغيل دائم)
        val radarBtn = Button(this)
        radarBtn.text = "تشغيل الرادار (يعمل في الخلفية) 📡"
        radarBtn.setBackgroundColor(Color.parseColor("#ff6b6b"))
        radarBtn.setTextColor(Color.WHITE)

        // 3. زر إنشاء الكارت
        val shareBtn = Button(this)
        shareBtn.text = "فتح الكارت ومشاركته 🚀"
        shareBtn.setBackgroundColor(Color.parseColor("#007bff"))
        shareBtn.setTextColor(Color.WHITE)

        // منطق زر المشاركة
        shareBtn.setOnClickListener {
            val name = nameInput.text.toString()
            if(name.isNotEmpty()){
                val finalUrl = "$myWebsiteUrl?name=$name&job=${jobInput.text}&phone=${phoneInput.text}&address=${addressInput.text}&fb=${fbInput.text}&img=${imgLinkInput.text}"
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl)))
            } else {
                Toast.makeText(this, "اكتب اسمك أولاً", Toast.LENGTH_SHORT).show()
            }
        }

        // منطق زر الرادار (تشغيل الخدمة)
        radarBtn.setOnClickListener {
            if (!isRadarOn) {
                // طلب الصلاحيات الشاملة
                val permissions = mutableListOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.BLUETOOTH_ADVERTISE,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
                )
                // إضافة صلاحية الخلفية للأندرويد الحديث
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    permissions.add(Manifest.permission.FOREGROUND_SERVICE)
                }

                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(permissions.toTypedArray(), 1)
                    return@setOnClickListener
                }

                // تشغيل الخدمة الدائمة
                val serviceIntent = Intent(this, RadarService::class.java)
                serviceIntent.putExtra("MY_NAME", nameInput.text.toString())
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent)
                } else {
                    startService(serviceIntent)
                }

                radarBtn.text = "الرادار يعمل الآن (يمكنك الخروج) 🔄"
                radarBtn.setBackgroundColor(Color.parseColor("#20bf6b"))
                isRadarOn = true
            } else {
                // إيقاف الخدمة
                val serviceIntent = Intent(this, RadarService::class.java)
                stopService(serviceIntent)
                
                radarBtn.text = "تشغيل الرادار (يعمل في الخلفية) 📡"
                radarBtn.setBackgroundColor(Color.parseColor("#ff6b6b"))
                isRadarOn = false
            }
        }

        layout.addView(imgLinkInput)
        layout.addView(nameInput)
        layout.addView(jobInput)
        layout.addView(phoneInput)
        layout.addView(addressInput)
        layout.addView(fbInput)
        layout.addView(radarBtn)
        layout.addView(shareBtn)

        scrollView.addView(layout)
        setContentView(scrollView)
    }
}
