package edu.umg.appcontrolgastos

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.umg.appcontrolgastos.databinding.ItemTransactionBinding
import umg.edu.gt.domain.model.Category
import umg.edu.gt.domain.model.Transaction
import umg.edu.gt.domain.model.TransactionType
import java.util.Locale

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

    class TransactionViewHolder(private val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Transaction) {
            binding.tvDescription.text = item.description
            binding.tvDate.text = item.date

            val formattedCategory = item.category.name
                .lowercase(Locale.getDefault())
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

            binding.tvCategoryName.text = formattedCategory
            binding.tvCategoryInitial.text = formattedCategory.take(1)

            applyCategoryStyle(item.category)

            binding.tvBadgeImpulse.visibility = if (item.isImpulse) View.VISIBLE else View.GONE

            val isIncome = item.type == TransactionType.INCOME
            val prefix = if (isIncome) "+" else "-"
            binding.tvAmount.text = String.format(Locale.US, "%s Q %.2f", prefix, item.amount)
            binding.tvAmount.setTextColor(
                if (isIncome) "#28A745".toColorInt() else "#212529".toColorInt()
            )
        }

        private fun applyCategoryStyle(category: Category) {
            val (bgColor, textColor) = when (category.name) {
                "FOOD" -> Pair("#FFF4E6", "#FD7E14")
                "TRANSPORT" -> Pair("#E7F5FF", "#1C7ED6")
                "ENTERTAINMENT" -> Pair("#F3F0FF", "#7950F2")
                "HEALTH" -> Pair("#E6FCF5", "#0CA678")
                "STUDIES" -> Pair("#EBFBEE", "#40C057")
                "SALARY" -> Pair("#E6FCF5", "#20C997")
                else -> Pair("#F1F3F5", "#495057")
            }

            val shape = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(bgColor.toColorInt())
            }
            binding.iconContainer.background = shape
            binding.tvCategoryInitial.setTextColor(textColor.toColorInt())
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean =
            oldItem == newItem
    }
}