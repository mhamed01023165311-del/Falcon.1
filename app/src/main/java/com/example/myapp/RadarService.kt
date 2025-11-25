package com.example.myapp

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.*
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.ParcelUuid
import android.widget.Toast
import java.util.UUID

class RadarService : Service() {

    // نفس الرقم التعريفي المستخدم في التطبيق
    val SERVICE_UUID = ParcelUuid(UUID.fromString("CDB7950D-73F1-4D4D-8E47-C090502DBD63"))
    var advertiser: BluetoothLeAdvertiser? = null
    var scanner: BluetoothLeScanner? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 1. إنشاء قناة الإشعارات (مطلوب للأندرويد الحديث)
        createNotificationChannel()

        // 2. تصميم الإشعار الثابت الذي سيظهر في الأعلى
        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, "RadarChannel")
                .setContentTitle("رادار الصقر يعمل 📡")
                .setContentText("جاري بث إشارتك والبحث عن الآخرين...")
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .build()
        } else {
            Notification.Builder(this)
                .setContentTitle("رادار الصقر يعمل")
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .build()
        }

        // 3. تشغيل الخدمة في وضع الـ Foreground (عشان النظام ميقفلهاش)
        startForeground(1, notification)

        // 4. استلام الاسم وبدء البلوتوث
        val myName = intent?.getStringExtra("MY_NAME") ?: "User"
        startBluetoothLogic(myName)

        // معناه: لو النظام قفل الخدمة بالغلط، يرجع يشغلها تاني لوحده
        return START_STICKY
    }

    private fun startBluetoothLogic(name: String) {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        advertiser = adapter.bluetoothLeAdvertiser
        scanner = adapter.bluetoothLeScanner

        // إعدادات الإرسال
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(false)
            .build()

        val shortName = if (name.length > 8) name.substring(0, 8) else name
        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .addServiceUuid(SERVICE_UUID)
            .addServiceData(SERVICE_UUID, shortName.toByteArray(Charsets.UTF_8))
            .build()

        advertiser?.startAdvertising(settings, data, object : AdvertiseCallback() {})

        // إعدادات البحث
        val filter = ScanFilter.Builder().setServiceUuid(SERVICE_UUID).build()
        val scanSettings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()

        scanner?.startScan(listOf(filter), scanSettings, object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                if (result != null && result.rssi > -75) { // مسافة 3 متر تقريباً
                    val dataMap = result.scanRecord?.serviceData
                    val nameBytes = dataMap?.get(SERVICE_UUID)
                    if (nameBytes != null) {
                        val foundName = String(nameBytes)
                        // إظهار رسالة (Toast) حتى لو التطبيق مقفول
                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(applicationContext, "شخص قريب: $foundName", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        })
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("RadarChannel", "Radar Service", NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // تنظيف البلوتوث عند الإيقاف
        advertiser?.stopAdvertising(object : AdvertiseCallback() {})
        scanner?.stopScan(object : ScanCallback() {})
        Toast.makeText(this, "تم إيقاف الرادار", Toast.LENGTH_SHORT).show()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
