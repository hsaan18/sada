package com.bench.wificrack

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var wifiManager: WifiManager
    private lateinit var adapter: WifiAdapter
    private val networks = mutableListOf<ScanResult>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        val rv = findViewById<RecyclerView>(R.id.rvNetworks)
        adapter = WifiAdapter(networks) { result ->
            val intent = Intent(this, CaptureActivity::class.java)
            intent.putExtra("BSSID", result.BSSID)
            intent.putExtra("SSID", result.SSID)
            intent.putExtra("CAPS", result.capabilities)
            startActivity(intent)
        }
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        findViewById<Button>(R.id.btnScan).setOnClickListener { startScan() }

        requestPerms()
    }

    private fun requestPerms() {
        val needed = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
        )
        val missing = needed.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty())
            ActivityCompat.requestPermissions(this, missing.toTypedArray(), 1)
        else
            startScan()
    }

    private fun startScan() {
        val filter = IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                unregisterReceiver(this)
                val results = wifiManager.scanResults
                networks.clear()
                networks.addAll(results.sortedByDescending { it.level })
                adapter.notifyDataSetChanged()
                findViewById<TextView>(R.id.tvStatus).text =
                    "Found ${networks.size} networks"
            }
        }, filter)
        @Suppress("DEPRECATION")
        wifiManager.startScan()
        findViewById<TextView>(R.id.tvStatus).text = "Scanning..."
    }
}
