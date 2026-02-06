package com.chandra.practice.deviceinfo.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chandra.practice.deviceinfo.R
import com.google.android.material.button.MaterialButton

class HorizontalButtonAdapter(
    private val items: List<String>,
    private val onClick: (String) -> Unit
    ) : RecyclerView.Adapter<HorizontalButtonAdapter.ButtonViewHolder>() {

    inner class ButtonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val button: MaterialButton = itemView.findViewById(R.id.categoryButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ButtonViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_button, parent, false)
        return ButtonViewHolder(view)
    }

    override fun onBindViewHolder(holder: ButtonViewHolder, position: Int) {
        val item = items[position]
        holder.button.text = item
        holder.button.setOnClickListener {
            onClick(item)
        }
    }

    override fun getItemCount(): Int = items.size
}