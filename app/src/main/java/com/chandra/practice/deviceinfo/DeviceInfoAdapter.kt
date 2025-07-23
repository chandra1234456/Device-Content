package com.chandra.practice.deviceinfo

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView

class DeviceInfoAdapter(private val items: List<DeviceInfoItem>) :
    RecyclerView.Adapter<DeviceInfoAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val label = itemView.findViewById<MaterialTextView>(R.id.labelTextView)
        val value = itemView.findViewById<MaterialTextView>(R.id.valueTextView)
        val imageButton = itemView.findViewById<AppCompatImageButton>(R.id.ibCopy)
    }

    override fun onCreateViewHolder(parent: ViewGroup , viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_device_info_items, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.label.text = item.label
        holder.value.text = item.value
        holder.imageButton.setOnClickListener {
            copyToClipboard(holder.itemView.context, item.label, item.value)
        }
    }
    private fun copyToClipboard(context: Context , label: String , value: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, value)
        clipboard.setPrimaryClip(clip)

        Toast.makeText(context, "Copied: $value", Toast.LENGTH_SHORT).show()
    }

}
