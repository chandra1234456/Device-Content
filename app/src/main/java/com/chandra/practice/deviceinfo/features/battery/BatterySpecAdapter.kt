package com.chandra.practice.deviceinfo.features.battery

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chandra.practice.deviceinfo.databinding.ItemSpecBinding

class BatterySpecAdapter(
    private val specList: List<SpecItem>
) : RecyclerView.Adapter<BatterySpecAdapter.SpecViewHolder>() {

    inner class SpecViewHolder(
        private val binding: ItemSpecBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SpecItem) {
            binding.ivIcon.setImageDrawable(item.icon)
            binding.tvTitle.text = item.title
            binding.tvValue.text = item.value
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SpecViewHolder {

        val binding = ItemSpecBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return SpecViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: SpecViewHolder,
        position: Int
    ) {
        holder.bind(specList[position])
    }

    override fun getItemCount(): Int = specList.size
}