package cn.com.zt.snaphelper

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import cn.com.zt.snaphelper.databinding.ItemBinding

class SquareAdapter(cxt: Context) : RecyclerView.Adapter<SquareHolder>() {
    private var context = cxt
    private val blackColor = context.getColor(android.R.color.black)
    private val whiteColor = context.getColor(android.R.color.white)
    private var itemClickListener: OnItemClickListener? = null

    fun setItemClickListener(l:OnItemClickListener){
        this.itemClickListener = l
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SquareHolder {
        val binding = ItemBinding.inflate(LayoutInflater.from(context))
        val squareHolder = SquareHolder(binding)
        squareHolder.itemView.setOnClickListener {
            itemClickListener?.onItemClick(it.tag as Int)
        }
        return squareHolder
    }

    override fun onBindViewHolder(holder: SquareHolder, position: Int) {
        holder.bind.textView.text = position.toString()
        if (position % 2 == 0) {
            holder.bind.textView.setTextColor(blackColor)
            holder.bind.textView.setBackgroundResource(R.color.design_default_color_secondary)
        } else {
            holder.bind.textView.setTextColor(whiteColor)
            holder.bind.textView.setBackgroundResource(R.color.design_default_color_on_secondary)
        }
        holder.itemView.tag = position
    }

    override fun getItemCount(): Int {
        return 20
    }
}

class SquareHolder(binding: ItemBinding) : RecyclerView.ViewHolder(binding.root) {
    var bind = binding;
}

interface OnItemClickListener {
    fun onItemClick(position: Int)
}