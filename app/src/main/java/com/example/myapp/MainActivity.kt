package com.example.myapp

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // إنشاء واجهة بسيطة برمجياً (بدون XML لتسهيل الأمور عليك)
        val layout = android.widget.LinearLayout(this)
        layout.orientation = android.widget.LinearLayout.VERTICAL
        layout.gravity = android.view.Gravity.CENTER
        
        val btnShare = Button(this)
        btnShare.text = "مشاركة بياناتي (رادار)"
        
        val btnScan = Button(this)
        btnScan.text = "البحث عن أشخاص"

        layout.addView(btnShare)
        layout.addView(btnScan)
        setContentView(layout)

        btnShare.setOnClickListener {
            Toast.makeText(this, "جاري تفعيل الرادار...", Toast.LENGTH_SHORT).show()
            // هنا سيتم استدعاء كود البلوتوث لاحقاً
        }

        btnScan.setOnClickListener {
             Toast.makeText(this, "جاري البحث...", Toast.LENGTH_SHORT).show()
        }
    }
}
