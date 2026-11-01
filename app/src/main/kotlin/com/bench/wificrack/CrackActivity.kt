package com.bench.wificrack

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class CrackActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crack)

        val etHash = findViewById<EditText>(R.id.etHashFile)
        val etWord = findViewById<EditText>(R.id.etWordlist)
        val tvOut  = findViewById<TextView>(R.id.tvCrackOut)
        val btnRun = findViewById<Button>(R.id.btnRunCrack)

        etHash.setText("/sdcard/capture.hc22000")
        etWord.setText("/sdcard/rockyou.txt")

        btnRun.setOnClickListener {
            val hashFile = etHash.text.toString()
            val wordlist = etWord.text.toString()

            // hashcat -m 22000: WPA-PBKDF2-PMKID+EAPOL unified (hashcat 6.2+)
            // convert pcap → hc22000: hcxpcapngtool -o out.hc22000 capture.pcap
            val cmd = "su -c \"/data/local/tmp/hashcat -m 22000 $hashFile $wordlist --status\""

            Thread {
                try {
                    val proc = Runtime.getRuntime().exec(arrayOf("sh", "-c", cmd))
                    val out  = proc.inputStream.bufferedReader().readText()
                    val err  = proc.errorStream.bufferedReader().readText()
                    proc.waitFor()
                    runOnUiThread {
                        tvOut.text = out.ifEmpty { err }
                    }
                } catch (e: Exception) {
                    runOnUiThread { tvOut.text = "ERR: ${e.message}" }
                }
            }.start()
        }
    }
}
