package com.spendguard.app.data.parser

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class NotificationParserTest {
    @Test
    fun parsesBkashDebit() {
        val parsed = NotificationParser.parse(
            "com.bKash.customerapp",
            "bKash",
            "Tk 450.00 has been paid to Grocery Mart.",
        )
        assertThat(parsed?.amount).isEqualTo(450.00)
        assertThat(parsed?.isExpense).isTrue()
    }

    @Test
    fun parsesInrDebit() {
        val parsed = NotificationParser.parse(
            "com.google.android.apps.nbu.paisa.user",
            "Google Pay",
            "Rs.1,250 spent at Cafe.",
        )
        assertThat(parsed?.amount).isEqualTo(1250.0)
    }

    @Test
    fun ignoresOtp() {
        val parsed = NotificationParser.parse(
            "com.android.mms",
            "Bank",
            "Your OTP is 452190. Do not share.",
        )
        assertThat(parsed).isNull()
    }

    @Test
    fun ignoresCredits() {
        val parsed = NotificationParser.parse(
            "com.bank",
            "Bank",
            "BDT 5000 credited to your account.",
        )
        assertThat(parsed).isNull()
    }
}
