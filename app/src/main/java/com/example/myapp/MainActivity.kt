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
    
    var isRadarOn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scrollView = ScrollView(this)
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.gravity = Gravity.CENTER
        layout.setPadding(40, 40, 40, 40)
        layout.setBackgroundColor(Color.parseColor("#F5F7FA"))

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

        val imgLinkInput = createInput("رابط صورتك الشخصية (Link)")
        val nameInput = createInput("الاسم")
        val jobInput = createInput("الوظيفة")
        val phoneInput = createInput("رقم الهاتف")
        val addressInput = createInput("العنوان")
        val fbInput = createInput("رابط فيسبوك")

        val radarBtn = Button(this)
        radarBtn.text = "تشغيل الرادار (يعمل في الخلفية) 📡"
        radarBtn.setBackgroundColor(Color.parseColor("#ff6b6b"))
        radarBtn.setTextColor(Color.WHITE)

        val shareBtn = Button(this)
        shareBtn.text = "فتح الكارت ومشاركته 🚀"
        shareBtn.setBackgroundColor(Color.parseColor("#007bff"))
        shareBtn.setTextColor(Color.WHITE)

        shareBtn.setOnClickListener {
            val name = nameInput.text.toString()
            if(name.isNotEmpty()){
                val finalUrl = "$myWebsiteUrl?name=$name&job=${jobInput.text}&phone=${phoneInput.text}&address=${addressInput.text}&fb=${fbInput.text}&img=${imgLinkInput.text}"
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl)))
            } else {
                Toast.makeText(this, "اكتب اسمك أولاً", Toast.LENGTH_SHORT).show()
            }
        }

        radarBtn.setOnClickListener {
            if (!isRadarOn) {
                // قائمة الصلاحيات المطلوبة
                val permissions = mutableListOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.BLUETOOTH_ADVERTISE,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
                )
                // إضافة صلاحية "الخلفية" و "الإشعارات" للأندرويد الحديث
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    permissions.add(Manifest.permission.FOREGROUND_SERVICE)
                }
                // ده الكود الجديد اللي هيحل المشكلة (طلب إذن الإشعار)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissions.add(Manifest.permission.POST_NOTIFICATIONS)
                }

                // التحقق وطلب الصلاحيات
                var allGranted = true
                for (perm in permissions) {
                    if (checkSelfPermission(perm) != PackageManager.PERMISSION_GRANTED) {
                        allGranted = false
                        break
                    }
                }

                if (!allGranted) {
                    requestPermissions(permissions.toTypedArray(), 1)
                    return@setOnClickListener
                }

                // تشغيل الخدمة
                startRadarService(nameInput.text.toString())
                
                radarBtn.text = "الرادار يعمل الآن (الإشعار ظاهر بالأعلى) 🔄"
                radarBtn.setBackgroundColor(Color.parseColor("#20bf6b"))
                isRadarOn = true
            } else {
                stopRadarService()
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

    private fun startRadarService(name: String) {
        val serviceIntent = Intent(this, RadarService::class.java)
        serviceIntent.putExtra("MY_NAME", name)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    private fun stopRadarService() {
        val serviceIntent = Intent(this, RadarService::class.java)
        stopService(serviceIntent)
    }
}
