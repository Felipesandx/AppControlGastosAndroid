package edu.umg.appcontrolgastos

import umg.edu.gt.domain.model.Category
import java.util.Locale

data class ParsedTransaction(
    val description: String,
    val amount: Double,
    val category: Category
)

object QuickAddParser {
    // Detecta patrones como "Cena 45", "120 gasolina", "uber 35.50"
    private val numberRegex = Regex("""(?:\$|Q)?\s*(\d+(?:[.,]\d{1,2})?)""")

    fun parse(input: String): ParsedTransaction? {
        val trimmed = input.trim()
        val match = numberRegex.findAll(trimmed).lastOrNull() ?: return null

        val amountStr = match.groupValues[1].replace(",", ".")
        val amount = amountStr.toDoubleOrNull() ?: return null

        // Remueve el número del texto para dejar solo el concepto
        val description = trimmed.removeRange(match.range).trim().replace(Regex("""\s+"""), " ")
        if (description.isEmpty()) return null

        val category = deduceCategory(description)

        return ParsedTransaction(
            description = description.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
            amount = amount,
            category = category
        )
    }

    private fun deduceCategory(text: String): Category {
        val lower = text.lowercase(Locale.getDefault())
        return when {
            listOf("comida", "almuerzo", "cena", "tacos", "cafe", "café", "pizza", "restaurante", "super").any { lower.contains(it) } -> Category.FOOD
            listOf("uber", "gasolina", "gas", "bus", "taxi", "parqueo", "peaje").any { lower.contains(it) } -> Category.TRANSPORT
            listOf("cine", "juego", "salida", "fiesta", "bar", "steam").any { lower.contains(it) } -> Category.ENTERTAINMENT
            listOf("medicina", "pastillas", "antibioticos", "jarabe", "pildoras", "consulta médica").any { lower.contains(it) } -> Category.HEALTH
            else -> Category.OTHER
        }
    }
}