package com.miapp.custodio2.Adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.miapp.custodio2.R

class Adapter(private val dataList: List<CardViewData>, private val listener: OnItemClickListener) : RecyclerView.Adapter<Adapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) :  RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val iconView: ImageView = itemView.findViewById(R.id.button_icon)
        val nameView: TextView = itemView.findViewById(R.id.button_name)
        val bc: RelativeLayout = itemView.findViewById(R.id.bc_icon)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position:Int = adapterPosition
            val name:String = nameView.text.toString()

            if (position != RecyclerView.NO_POSITION) {
                listener.onItemClick(position, name)
            }
        }
    }

    interface OnItemClickListener{
        fun onItemClick(position: Int, name: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.layout_buttons, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = dataList[position]
        val url:String = currentItem.icon


        if(currentItem == dataList[6]){ //el panico es el 7 desde el 1 y 6 desde el 0
            //holder.iconView.setBackgroundResource(R.drawable.ubi_white)
            holder.bc.setBackgroundColor(Color.RED)
            holder.nameView.setTextColor(Color.WHITE)
            holder.iconView.setImageResource(R.drawable.ubi_white)
            holder.nameView.text = currentItem.name
        } else {
            Glide.with(holder.itemView.context)
                .load(url)
                .into(holder.iconView)
            holder.nameView.text = currentItem.name
        }

    }

    override fun getItemCount() = dataList.size

}