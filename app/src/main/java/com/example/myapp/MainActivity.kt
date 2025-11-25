package com.example.myapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import android.graphics.Color

class MainActivity : Activity() {

    // رابط موقعك اللي عملناه (غيرت اسم المستخدم حسب الرابط اللي انت بعتهولي)
    val myWebsiteUrl = "https://mhamed01023165311-del.github.io/Falcon.1/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. تصميم الشاشة (Layout)
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.gravity = Gravity.CENTER
        layout.setPadding(50, 50, 50, 50)
        layout.setBackgroundColor(Color.parseColor("#F0F0F0"))

        // 2. خانة إدخال الاسم
        val nameInput = EditText(this)
        nameInput.hint = "ادخل اسمك هنا"
        nameInput.setBackgroundColor(Color.WHITE)
        nameInput.setPadding(30, 30, 30, 30)
        
        // 3. خانة إدخال الوظيفة
        val jobInput = EditText(this)
        jobInput.hint = "ادخل وظيفتك"
        jobInput.setBackgroundColor(Color.WHITE)
        jobInput.setPadding(30, 30, 30, 30)
        
        // مسافات بين العناصر
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 20, 0, 20)
        nameInput.layoutParams = params
        jobInput.layoutParams = params

        // 4. زر المشاركة
        val shareBtn = Button(this)
        shareBtn.text = "إنشاء الكارت ومشاركته 🚀"
        shareBtn.setBackgroundColor(Color.parseColor("#007bff"))
        shareBtn.setTextColor(Color.WHITE)
        shareBtn.layoutParams = params

        // 5. ماذا يحدث عند الضغط؟
        shareBtn.setOnClickListener {
            val name = nameInput.text.toString()
            val job = jobInput.text.toString()

            if (name.isNotEmpty() && job.isNotEmpty()) {
                // تكوين الرابط الذكي
                val finalUrl = "$myWebsiteUrl?name=$name&job=$job"
                
                // فتح الرابط في المتصفح (أو إرساله لشخص)
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl))
                startActivity(browserIntent)
                
                Toast.makeText(this, "جاري فتح الكارت...", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "من فضلك اكتب البيانات الأول", Toast.LENGTH_SHORT).show()
            }
        }

        // إضافة العناصر للشاشة
        layout.addView(nameInput)
        layout.addView(jobInput)
        layout.addView(shareBtn)

        setContentView(layout)
    }
}
