package com.bench.wificrack

import android.net.wifi.ScanResult
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class WifiAdapter(
    private val items: List<ScanResult>,
    private val onClick: (ScanResult) -> Unit
) : RecyclerView.Adapter<WifiAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvSSID  : TextView = v.findViewById(R.id.tvSSID)
        val tvBSSID : TextView = v.findViewById(R.id.tvBSSID)
        val tvSig   : TextView = v.findViewById(R.id.tvSignal)
        val tvSec   : TextView = v.findViewById(R.id.tvSecurity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_wifi, parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val r = items[position]
        holder.tvSSID.text  = r.SSID.ifEmpty { "<hidden>" }
        holder.tvBSSID.text = r.BSSID
        holder.tvSig.text   = "${r.level} dBm"
        holder.tvSec.text   = when {
            r.capabilities.contains("WPA3") -> "WPA3"
            r.capabilities.contains("WPA2") -> "WPA2"
            r.capabilities.contains("WPA")  -> "WPA"
            r.capabilities.contains("WEP")  -> "WEP"
            else -> "OPEN"
        }
        holder.itemView.setOnClickListener { onClick(r) }
    }
}
