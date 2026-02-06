package com.chandra.practice.deviceinfo.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chandra.practice.deviceinfo.R
import com.chandra.practice.deviceinfo.data.QuickStat

class QuickStatsAdapter(private val stats: List<QuickStat>) :
    RecyclerView.Adapter<QuickStatsAdapter.ViewHolder>() {
    
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
       // val statIcon: ImageView = view.findViewById(R.id.statIcon)
       // val statValue: TextView = view.findViewById(R.id.statValue)
        val statLabel: TextView = view.findViewById(R.id.categoryButton)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_button, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val stat = stats[position]
        //holder.statIcon.setImageResource(stat.iconRes)
       // holder.statValue.text = stat.value
        holder.statLabel.text = stat.label
    }
    
    override fun getItemCount() = stats.size
}