package edu.umg.appcontrolgastos

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.umg.appcontrolgastos.databinding.ItemTransactionBinding
import umg.edu.gt.domain.model.Transaction
import umg.edu.gt.domain.model.TransactionType

class TransactionAdapter(
    private val onItemClick: (Transaction) -> Unit
) : ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    fun getTransactionAt(position: Int): Transaction = getItem(position)

    inner class TransactionViewHolder(private val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Transaction) {
            val prefix = if (item.type == TransactionType.INCOME) "+ " else "- "
            binding.tvDescription.text = "${item.description} • ${item.category.name.lowercase().replaceFirstChar { it.titlecase() }}"
            binding.tvAmount.text = "$prefix Q ${String.format("%.2f", item.amount)}"

            val colorRes = if (item.type == TransactionType.INCOME) {
                android.graphics.Color.parseColor("#28A745")
            } else {
                android.graphics.Color.parseColor("#DC3545")
            }
            binding.tvAmount.setTextColor(colorRes)
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean =
            oldItem == newItem
    }
}