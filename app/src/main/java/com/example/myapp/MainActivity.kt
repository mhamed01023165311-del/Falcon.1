package com.example.myapp

import android.os.Bundle
import android.app.Activity  // تغيير مهم: استدعاء المكتبة الأساسية
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import android.view.Gravity

// تغيير مهم: شلنا AppCompatActivity وحطينا Activity بس
class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // بناء الشاشة
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.gravity = Gravity.CENTER
        
        val button = Button(this)
        button.text = "مرحباً بك في تطبيق الصقر"
        
        button.setOnClickListener {
            Toast.makeText(this, "التطبيق يعمل بنجاح 100%", Toast.LENGTH_LONG).show()
        }

        layout.addView(button)
        setContentView(layout)
    }
}
