package com.chandra.practice.deviceinfo.features.appsInfo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chandra.practice.deviceinfo.databinding.ItemAppBinding

class AppsAdapter(
    private var list: List<AppInfo>,
    private val onItemClick: (AppInfo) -> Unit
) : RecyclerView.Adapter<AppsAdapter.AppViewHolder>() {

    inner class AppViewHolder(val binding: ItemAppBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = ItemAppBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AppViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = list[position]

        holder.binding.imgIcon.setImageDrawable(app.icon)
        holder.binding.tvName.text = app.name
        holder.binding.tvPackage.text = app.packageName
        holder.binding.tvMeta.text =
            "V ${app.version} • ${formatSize(app.sizeBytes)}"

        holder.binding.tvType.text =
            if (app.isSystemApp) "SYSTEM" else "USER"

        holder.itemView.setOnClickListener {
            onItemClick(app)
        }
    }

    override fun getItemCount(): Int = list.size

    fun updateList(newList: List<AppInfo>) {
        list = newList
        notifyDataSetChanged()
    }

    companion object {
        fun formatSize(size: Long): String {
            val kb = size / 1024.0
            val mb = kb / 1024.0
            val gb = mb / 1024.0

            return when {
                gb >= 1 -> String.format("%.2f GB", gb)
                mb >= 1 -> String.format("%.2f MB", mb)
                kb >= 1 -> String.format("%.2f KB", kb)
                else -> "$size B"
            }
        }
    }
}