package com.example.meuspedidosstore.adapter

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.util.isNotEmpty
import androidx.recyclerview.widget.RecyclerView
import com.example.meuspedidosstore.R
import com.example.meuspedidosstore.data.Archived
import com.example.meuspedidosstore.databinding.ItemOrderBinding
import com.example.meuspedidosstore.util.DataStore
import com.example.meuspedidosstore.util.DateUtil

class ArchivedAdapter(
    private val context: Context,
    val Archived: List<Archived>,
) :
    RecyclerView.Adapter<ArchivedAdapter.OrderViewHolder>() {

    private val selectedOrders = SparseBooleanArray()
    private val dataStore = DataStore()
    private val dateUtil = DateUtil()
    private val logoStore: TypedArray by lazy {
        context.resources.obtainTypedArray(R.array.logo_stores)
    }
    var archivedList = Archived

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val itemOrderBinding =
            ItemOrderBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return OrderViewHolder(itemOrderBinding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(archivedList[position])
        holder.itemView.setOnClickListener {
            if (selectedOrders.isNotEmpty())
                onItemClick?.invoke(position)
        }
        holder.itemView.setOnLongClickListener {
            onItemLongClick?.invoke(position)
            return@setOnLongClickListener true
        }
    }

    override fun getItemCount(): Int = archivedList.size

    fun setData(archived: List<Archived>) {
        this.archivedList = archived
        notifyDataSetChanged()
    }

    var onItemClick: ((Int) -> Unit)? = null
    var onItemLongClick: ((Int) -> Unit)? = null

    inner class OrderViewHolder(private val itemOrderBinding: ItemOrderBinding) :
        RecyclerView.ViewHolder(itemOrderBinding.root) {
        fun bind(archived: Archived) {
            itemOrderBinding.viewOrder.setBackgroundColor(Color.parseColor("#FF018786"))
            val logo = dataStore.logo(archived.icon)
            val date = dateUtil.getTreatedDateTime(archived.date)
            itemOrderBinding.ivStore.setImageDrawable(logoStore.getDrawable(logo))
            itemOrderBinding.tvNameStore.text = archived.nameStore
            itemOrderBinding.tvOrderNumber.text = archived.number
            itemOrderBinding.tvStatus.text = archived.status
            itemOrderBinding.tvDateTime.text = date
        }
    }
}