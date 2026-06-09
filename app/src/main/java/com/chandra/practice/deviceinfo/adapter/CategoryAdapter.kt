package com.chandra.practice.deviceinfo.adapter

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.RecyclerView
import com.chandra.practice.deviceinfo.R
import com.chandra.practice.deviceinfo.data.CategoryItem

class CategoryAdapter(
    private val list: List<CategoryItem>,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.title)
        val icon: ImageView = view.findViewById(R.id.icon)
        val iconContainer: View = view.findViewById(R.id.iconContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        val context = holder.itemView.context
        
        holder.title.text = item.title
        holder.icon.setImageResource(item.icon)

        // Programmatically generate the dynamic pastel background for the icon container
        try {
            val tintColor = ContextCompat.getColor(context, item.colorRes)
            val alphaColor = ColorUtils.setAlphaComponent(tintColor, 38) // ~15% opacity (38 out of 255)
            
            val density = context.resources.displayMetrics.density
            val cornerRadiusPx = 16f * density // 16dp rounded corners
            
            val backgroundDrawable = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = cornerRadiusPx
                setColor(alphaColor)
            }
            holder.iconContainer.background = backgroundDrawable
            holder.icon.setColorFilter(tintColor)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Tactile micro-animation: scale card slightly when clicked, then trigger navigation
        holder.itemView.setOnClickListener {
            holder.itemView.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(80)
                .withEndAction {
                    holder.itemView.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(80)
                        .withEndAction {
                            onClick(item.title)
                        }
                        .start()
                }
                .start()
        }
    }
}