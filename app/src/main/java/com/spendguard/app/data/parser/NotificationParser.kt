package com.spendguard.app.data.parser

import java.util.Locale

data class ParsedTransaction(
    val amount: Double,
    val isExpense: Boolean,
    val merchant: String?,
)

object NotificationParser {
    private val expenseHints = listOf(
        "debit", "debited", "spent", "paid", "payment", "purchase", "withdrawn",
        "sent", "deducted", "charged", "transfer to", "bought", "pos",
    )
    private val incomeHints = listOf(
        "credit", "credited", "received", "refund", "deposited", "added",
        "cashback", "incoming",
    )
    private val skipHints = listOf(
        "otp", "one time password", "verification code", "is your code",
        "do not share", "login", "sign-in",
    )

    private val amountPatterns = listOf(
        Regex(
            """(?:tk\.?|bdt|rs\.?|inr|usd|eur|php|sgd|npr|pkr|aud|gbp|₹|৳|\$|€)\s*([0-9]{1,3}(?:,[0-9]{3})*(?:\.[0-9]{1,2})?|[0-9]+(?:\.[0-9]{1,2})?)""",
            RegexOption.IGNORE_CASE,
        ),
        Regex(
            """([0-9]{1,3}(?:,[0-9]{3})*(?:\.[0-9]{1,2})?|[0-9]+(?:\.[0-9]{1,2})?)\s*(?:tk\.?|bdt|rs\.?|inr|usd)""",
            RegexOption.IGNORE_CASE,
        ),
    )

    fun parse(packageName: String, title: String, text: String): ParsedTransaction? {
        if (packageName == "com.spendguard.app") return null
        val blob = "$title\n$text".lowercase(Locale.US)
        if (skipHints.any { blob.contains(it) }) return null

        val amount = extractAmount("$title $text") ?: return null
        if (amount <= 0.0) return null

        val isIncome = incomeHints.any { blob.contains(it) }
        val isExpense = expenseHints.any { blob.contains(it) }
        if (!isExpense && !isIncome) return null
        if (isIncome && !isExpense) return null

        val merchant = title.trim().ifBlank { null }?.take(80)
        return ParsedTransaction(amount = amount, isExpense = true, merchant = merchant)
    }

    internal fun extractAmount(raw: String): Double? {
        for (pattern in amountPatterns) {
            val match = pattern.find(raw) ?: continue
            val number = match.groupValues[1].replace(",", "")
            return number.toDoubleOrNull()
        }
        return null
    }
}
