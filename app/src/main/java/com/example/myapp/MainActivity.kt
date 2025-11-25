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
import android.view.View
import android.widget.*
import com.google.firebase.FirebaseApp
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class MainActivity : Activity() {

    // رابط موقعك
    val myWebsiteUrl = "https://mhamed01023165311-del.github.io/Falcon.1/"
    
    // متغيرات للصورة
    var selectedImageUri: Uri? = null
    var uploadedImageUrl: String = ""
    lateinit var uploadBtn: Button
    
    // حالة الرادار
    var isRadarOn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // تشغيل خدمات جوجل
        FirebaseApp.initializeApp(this)

        // --- التصميم ---
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

        // 1. زرار رفع الصورة (بدل ما تكتب الرابط)
        uploadBtn = Button(this)
        uploadBtn.text = "اختر صورتك الشخصية 📸"
        uploadBtn.setBackgroundColor(Color.parseColor("#6c5ce7"))
        uploadBtn.setTextColor(Color.WHITE)
        val paramsBtn = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        paramsBtn.setMargins(0, 15, 0, 15)
        uploadBtn.layoutParams = paramsBtn

        uploadBtn.setOnClickListener {
            // فتح الاستوديو لاختيار صورة
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, 100)
        }

        // 2. باقي الخانات
        val nameInput = createInput("الاسم")
        val jobInput = createInput("الوظيفة")
        val phoneInput = createInput("رقم الهاتف")
        val addressInput = createInput("العنوان")
        val fbInput = createInput("رابط فيسبوك")

        // 3. أزرار التحكم
        val radarBtn = Button(this)
        radarBtn.text = "تشغيل الرادار (يعمل في الخلفية) 📡"
        radarBtn.setBackgroundColor(Color.parseColor("#ff6b6b"))
        radarBtn.setTextColor(Color.WHITE)

        val shareBtn = Button(this)
        shareBtn.text = "فتح الكارت ومشاركته 🚀"
        shareBtn.setBackgroundColor(Color.parseColor("#007bff"))
        shareBtn.setTextColor(Color.WHITE)

        // --- المنطق ---

        // زر المشاركة
        shareBtn.setOnClickListener {
            val name = nameInput.text.toString()
            if(name.isNotEmpty()){
                // لو الصورة اترفعت، نستخدم الرابط بتاعها، لو لأ، نسيبها فاضية
                val finalImg = if(uploadedImageUrl.isNotEmpty()) uploadedImageUrl else ""
                
                val finalUrl = "$myWebsiteUrl?name=$name&job=${jobInput.text}&phone=${phoneInput.text}&address=${addressInput.text}&fb=${fbInput.text}&img=$finalImg"
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl)))
            } else {
                Toast.makeText(this, "اكتب اسمك أولاً", Toast.LENGTH_SHORT).show()
            }
        }

        // زر الرادار
        radarBtn.setOnClickListener {
            if (!isRadarOn) {
                if (!checkPermissions()) return@setOnClickListener
                startRadarService(nameInput.text.toString())
                radarBtn.text = "الرادار يعمل الآن 🔄"
                radarBtn.setBackgroundColor(Color.parseColor("#20bf6b"))
                isRadarOn = true
            } else {
                stopRadarService()
                radarBtn.text = "تشغيل الرادار (يعمل في الخلفية) 📡"
                radarBtn.setBackgroundColor(Color.parseColor("#ff6b6b"))
                isRadarOn = false
            }
        }

        layout.addView(uploadBtn) // الزر الجديد
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

    // --- كود رفع الصورة للسيرفر ---
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.data
            uploadBtn.text = "جاري الرفع... ⏳"
            uploadImageToFirebase()
        }
    }

    fun uploadImageToFirebase() {
        if (selectedImageUri == null) return

        // اسم عشوائي للصورة عشان الصور ما تدخلش في بعض
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedImageUri!!)
            .addOnSuccessListener {
                // تم الرفع بنجاح! نجيب الرابط بقى
                ref.downloadUrl.addOnSuccessListener { uri ->
                    uploadedImageUrl = uri.toString()
                    uploadBtn.text = "تم رفع الصورة بنجاح ✅"
                    uploadBtn.setBackgroundColor(Color.parseColor("#20bf6b"))
                    Toast.makeText(this, "تم الرفع! جاهز للمشاركة", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                uploadBtn.text = "فشل الرفع ❌"
                Toast.makeText(this, "تأكد من النت: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    // --- دوال الرادار والصلاحيات ---
    private fun checkPermissions(): Boolean {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) permissions.add(Manifest.permission.FOREGROUND_SERVICE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) permissions.add(Manifest.permission.POST_NOTIFICATIONS)

        var allGranted = true
        for (perm in permissions) {
            if (checkSelfPermission(perm) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false
                break
            }
        }
        if (!allGranted) {
            requestPermissions(permissions.toTypedArray(), 1)
            return false
        }
        return true
    }

    private fun startRadarService(name: String) {
        val serviceIntent = Intent(this, RadarService::class.java)
        serviceIntent.putExtra("MY_NAME", name)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(serviceIntent)
        else startService(serviceIntent)
    }

    private fun stopRadarService() {
        stopService(Intent(this, RadarService::class.java))
    }
}
