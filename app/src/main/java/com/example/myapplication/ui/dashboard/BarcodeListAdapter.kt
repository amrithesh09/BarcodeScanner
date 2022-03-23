package com.example.myapplication.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.BarcodeListItemBinding
import com.example.myapplication.model.BarCode

class HistoryDiffCallBack: DiffUtil.ItemCallback<BarCode>() {
    override fun areItemsTheSame(oldItem: BarCode, newItem: BarCode): Boolean {
        return newItem == oldItem
    }

    override fun areContentsTheSame(oldItem: BarCode, newItem: BarCode): Boolean {
        return newItem == oldItem
    }

}
class BarcodeListRecyclerAdapter:ListAdapter<BarCode, BarcodeListRecyclerAdapter.HistoryViewHolder>(HistoryDiffCallBack()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {

        val inflater = LayoutInflater.from(parent.context)
        val binding = BarcodeListItemBinding.inflate(inflater)
        return HistoryViewHolder(binding)
    }

       override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = getItem(position)
        holder.textViewDate.text = item.timeTaken.toString()
        holder.textViewType.text = item.type.toString()
        holder.textViewValue.text = item.value

        //holder.rootview.tag = item
    }


    inner class HistoryViewHolder(val binding: BarcodeListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val deleteIcon = binding.deleteIcon
        val textViewType = binding.textViewType
        val textViewValue = binding.textViewValue
        val textViewDate = binding.textViewDate

    }
}