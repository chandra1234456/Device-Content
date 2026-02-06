package com.chandra.practice.deviceinfo.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chandra.practice.deviceinfo.R
import com.chandra.practice.deviceinfo.data.DeviceInfoItem
import com.google.android.material.button.MaterialButton
import java.util.Locale

class DeviceInfoAdapter(
    private var data: List<DeviceInfoItem>,
    private val onCopyClick: (DeviceInfoItem) -> Unit
) : RecyclerView.Adapter<DeviceInfoAdapter.ViewHolder>(), Filterable {

    private var filteredData: List<DeviceInfoItem> = data

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val labelTextView: TextView = view.findViewById(R.id.labelTextView)
        val valueTextView: TextView = view.findViewById(R.id.valueTextView)
        val copyButton: MaterialButton = view.findViewById(R.id.copyButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_device_info_items, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = filteredData[position]
        holder.labelTextView.text = item.label
        holder.valueTextView.text = item.value

        holder.copyButton.setOnClickListener {
            onCopyClick(item)
        }
    }

    override fun getItemCount() = filteredData.size

    fun updateData(newData: List<DeviceInfoItem>) {
        data = newData
        filteredData = newData
        notifyDataSetChanged()
    }

    fun getAllData(): List<DeviceInfoItem> = data

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredList = mutableListOf<DeviceInfoItem>()
                if (constraint.isNullOrBlank()) {
                    filteredList.addAll(data)
                } else {
                    val filterPattern = constraint.toString().lowercase(Locale.getDefault())
                    data.forEach { item ->
                        if (item.label.lowercase(Locale.getDefault()).contains(filterPattern) ||
                            item.value.lowercase(Locale.getDefault()).contains(filterPattern)) {
                            filteredList.add(item)
                        }
                    }
                }
                val results = FilterResults()
                results.values = filteredList
                return results
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredData = results?.values as? List<DeviceInfoItem> ?: emptyList()
                notifyDataSetChanged()
            }
        }
    }
}