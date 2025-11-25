package com.example.myapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Toast
import android.graphics.Color

class MainActivity : Activity() {

    // رابط موقعك (تأكد إنه هو ده الرابط الصحيح بتاعك)
    val myWebsiteUrl = "https://mhamed01023165311-del.github.io/Falcon.1/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // عملنا ScrollView عشان لو البيانات كترت الشاشة تنزل لتحت
        val scrollView = ScrollView(this)
        
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.gravity = Gravity.CENTER
        layout.setPadding(50, 50, 50, 50)
        layout.setBackgroundColor(Color.parseColor("#F0F0F0"))
        
        // دالة مساعدة لإنشاء الخانات بسرعة
        fun createInput(hint: String): EditText {
            val input = EditText(this)
            input.hint = hint
            input.setBackgroundColor(Color.WHITE)
            input.setPadding(40, 40, 40, 40)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 15, 0, 15)
            input.layoutParams = params
            return input
        }

        // 1. إنشاء الخانات
        val nameInput = createInput("الاسم بالكامل")
        val jobInput = createInput("المسمى الوظيفي")
        val phoneInput = createInput("رقم الهاتف (مهم)")
        val addressInput = createInput("العنوان / السكن")
        val facebookInput = createInput("رابط الفيسبوك (اختياري)")

        // 2. زر المشاركة
        val shareBtn = Button(this)
        shareBtn.text = "إنشاء الكارت الشامل 🚀"
        shareBtn.setBackgroundColor(Color.parseColor("#007bff"))
        shareBtn.setTextColor(Color.WHITE)
        shareBtn.setPadding(30, 30, 30, 30)

        // 3. عند الضغط
        shareBtn.setOnClickListener {
            val name = nameInput.text.toString()
            val job = jobInput.text.toString()
            val phone = phoneInput.text.toString()
            val address = addressInput.text.toString()
            val facebook = facebookInput.text.toString()

            if (name.isNotEmpty()) {
                // تجميع كل البيانات في الرابط
                // لاحظ علامة & بنستخدمها عشان نفصل بين كل معلومة والتانية
                val finalUrl = "$myWebsiteUrl?name=$name&job=$job&phone=$phone&address=$address&fb=$facebook"
                
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl))
                startActivity(browserIntent)
            } else {
                Toast.makeText(this, "اكتب اسمك على الأقل", Toast.LENGTH_SHORT).show()
            }
        }

        // إضافة العناصر للشاشة
        layout.addView(nameInput)
        layout.addView(jobInput)
        layout.addView(phoneInput)
        layout.addView(addressInput)
        layout.addView(facebookInput)
        layout.addView(shareBtn)

        scrollView.addView(layout)
        setContentView(scrollView)
    }
}
