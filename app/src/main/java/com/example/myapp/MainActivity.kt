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
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class MainActivity : Activity() {

    // رابط موقعك
    val myWebsiteUrl = "https://mhamed01023165311-del.github.io/Falcon.1/"
    
    // متغيرات السيرفر
    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()
    
    // متغيرات الصورة
    var selectedImageUri: Uri? = null
    lateinit var uploadBtn: Button
    
    // حالة الرادار
    var isRadarOn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        // العناصر
        uploadBtn = Button(this)
        uploadBtn.text = "1. اختر صورتك الشخصية 📸"
        uploadBtn.setBackgroundColor(Color.parseColor("#6c5ce7"))
        uploadBtn.setTextColor(Color.WHITE)
        
        val nameInput = createInput("الاسم")
        val jobInput = createInput("الوظيفة")
        val phoneInput = createInput("رقم الهاتف")
        val addressInput = createInput("العنوان")
        val fbInput = createInput("رابط فيسبوك")

        val saveBtn = Button(this)
        saveBtn.text = "2. حفظ البيانات في السيرفر 💾"
        saveBtn.setBackgroundColor(Color.parseColor("#00b894"))
        saveBtn.setTextColor(Color.WHITE)

        val radarBtn = Button(this)
        radarBtn.text = "3. تشغيل الرادار 📡"
        radarBtn.setBackgroundColor(Color.parseColor("#ff6b6b"))
        radarBtn.setTextColor(Color.WHITE)

        val shareBtn = Button(this)
        shareBtn.text = "4. فتح الكارت ومشاركته 🚀"
        shareBtn.setBackgroundColor(Color.parseColor("#0984e3"))
        shareBtn.setTextColor(Color.WHITE)

        // --- الأزرار ---

        // 1. اختيار صورة
        uploadBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, 100)
        }

        // 2. حفظ في السيرفر (الخطوة الأهم)
        saveBtn.setOnClickListener {
            val name = nameInput.text.toString()
            if (name.isEmpty()) {
                Toast.makeText(this, "اكتب الاسم الأول", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            saveBtn.text = "جاري الحفظ... ⏳"
            
            // لو فيه صورة مختارة، نرفعها الأول
            if (selectedImageUri != null) {
                uploadImageAndSaveData(name, jobInput.text.toString(), phoneInput.text.toString(), addressInput.text.toString(), fbInput.text.toString())
            } else {
                // لو مفيش صورة، نحفظ البيانات بس
                saveDataToFirestore(name, jobInput.text.toString(), phoneInput.text.toString(), addressInput.text.toString(), fbInput.text.toString(), "")
            }
        }

        // 3. الرادار
        radarBtn.setOnClickListener {
            if (!isRadarOn) {
                if (!checkPermissions()) return@setOnClickListener
                startRadarService(nameInput.text.toString())
                radarBtn.text = "الرادار يعمل (إشعار بالأعلى) 🔄"
                radarBtn.setBackgroundColor(Color.parseColor("#20bf6b"))
                isRadarOn = true
            } else {
                stopRadarService()
                radarBtn.text = "3. تشغيل الرادار 📡"
                radarBtn.setBackgroundColor(Color.parseColor("#ff6b6b"))
                isRadarOn = false
            }
        }

        // 4. المشاركة
        shareBtn.setOnClickListener {
            val name = nameInput.text.toString()
            // هنا المفروض نجيب البيانات من السيرفر، بس للتسهيل هنبعتها في الرابط برضه
            val finalUrl = "$myWebsiteUrl?name=$name&job=${jobInput.text}&phone=${phoneInput.text}"
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl)))
        }

        layout.addView(uploadBtn)
        layout.addView(nameInput)
        layout.addView(jobInput)
        layout.addView(phoneInput)
        layout.addView(addressInput)
        layout.addView(fbInput)
        layout.addView(saveBtn)
        layout.addView(radarBtn)
        layout.addView(shareBtn)

        scrollView.addView(layout)
        setContentView(scrollView)
    }

    // --- وظائف السيرفر ---

    // رفع الصورة ثم حفظ البيانات
    fun uploadImageAndSaveData(name: String, job: String, phone: String, address: String, fb: String) {
        val filename = UUID.randomUUID().toString()
        val ref = storage.reference.child("images/$filename")

        ref.putFile(selectedImageUri!!)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { uri ->
                    // بعد ما الصورة اترفعت، نحفظ البيانات مع رابط الصورة
                    saveDataToFirestore(name, job, phone, address, fb, uri.toString())
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "فشل رفع الصورة (تأكد من تفعيل Storage): ${it.message}", Toast.LENGTH_LONG).show()
                // حتى لو الصورة فشلت، هنحاول نحفظ باقي البيانات
                saveDataToFirestore(name, job, phone, address, fb, "")
            }
    }

    // حفظ البيانات في قاعدة البيانات
    fun saveDataToFirestore(name: String, job: String, phone: String, address: String, fb: String, imgUrl: String) {
        // تجهيز البيانات
        val userMap = hashMapOf(
            "name" to name,
            "job" to job,
            "phone" to phone,
            "address" to address,
            "facebook" to fb,
            "image" to imgUrl
        )

        // الحفظ في مجموعة اسمها "Users"
        db.collection("Users").add(userMap)
            .addOnSuccessListener {
                Toast.makeText(this, "تم الحفظ في السيرفر بنجاح! ✅", Toast.LENGTH_LONG).show()
                findViewById<Button>(2).text = "تم الحفظ ✅" // تغيير نص الزرار (تقريبي)
            }
            .addOnFailureListener {
                Toast.makeText(this, "فشل الحفظ: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.data
            uploadBtn.text = "تم اختيار الصورة (اضغط حفظ)"
        }
    }

    // --- الرادار ---
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
            if (checkSelfPermission(perm) != PackageManager.PERMISSION_GRANTED) allGranted = false
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
