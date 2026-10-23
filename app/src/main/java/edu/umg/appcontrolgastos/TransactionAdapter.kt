package edu.umg.appcontrolgastos

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.umg.appcontrolgastos.databinding.ItemTransactionBinding
import umg.edu.gt.domain.model.Transaction
import umg.edu.gt.domain.model.TransactionType

class TransactionAdapter : RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    private var transactions: List<Transaction> = emptyList()

    fun submitList(list: List<Transaction>) {
        transactions = list
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemTransactionBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tx = transactions[position]
        with(holder.binding) {
            tvDescription.text = tx.description
            tvCategoryDate.text = "${tx.category.name} • ${tx.date}"

            // Colores dinámicos según el tipo de transacción
            if (tx.type == TransactionType.INCOME) {
                tvAmount.text = "+Q${tx.amount}"
                tvAmount.setTextColor(Color.parseColor("#28a745")) // Verde
                viewCategoryIcon.setBackgroundColor(Color.parseColor("#d4edda"))
            } else {
                tvAmount.text = "-Q${tx.amount}"
                tvAmount.setTextColor(Color.parseColor("#dc3545")) // Rojo
                viewCategoryIcon.setBackgroundColor(Color.parseColor("#f8d7da"))
            }
        }
    }

    override fun getItemCount() = transactions.size
}