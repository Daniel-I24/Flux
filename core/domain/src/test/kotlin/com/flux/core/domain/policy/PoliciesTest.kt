package com.flux.core.domain.policy

import com.flux.core.model.AccountLink
import com.flux.core.model.DailyRoutine
import com.flux.core.model.PhoneOwnership
import com.flux.core.model.TimeOfDay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PoliciesTest {
    @Test
    fun `phone ownership follows the stored installation id`() {
        assertEquals(PhoneOwnership.Unclaimed, PhoneOwnershipPolicy.evaluate(null, "me"))
        assertEquals(PhoneOwnership.Unclaimed, PhoneOwnershipPolicy.evaluate(AccountLink(null, null, null), "me"))
        assertEquals(PhoneOwnership.ThisPhone, PhoneOwnershipPolicy.evaluate(AccountLink("me", null, 1), "me"))
        assertEquals(PhoneOwnership.OtherPhone, PhoneOwnershipPolicy.evaluate(AccountLink("other", null, 1), "me"))
    }

    @Test
    fun `a watch owned by another account is rejected`() {
        assertEquals(
            WatchLinkDecision.WatchOwnedByOtherAccount,
            WatchLinkPolicy.decide("me", "w1", watchOwnerUid = "other", accountWatchNodeId = null)
        )
    }

    @Test
    fun `linking rules for free, same and replacement watches`() {
        assertEquals(WatchLinkDecision.Allowed, WatchLinkPolicy.decide("me", "w1", null, null))
        assertEquals(WatchLinkDecision.AlreadyLinkedHere, WatchLinkPolicy.decide("me", "w1", "me", "w1"))
        assertEquals(WatchLinkDecision.ReplacesPreviousWatch, WatchLinkPolicy.decide("me", "w2", null, "w1"))
    }

    @Test
    fun `confirmation codes always have six digits`() {
        assertEquals("000042", WatchLinkPolicy.confirmationCode(42))
        assertEquals(6, WatchLinkPolicy.confirmationCode(-123_456_789).length)
    }

    @Test
    fun `quiet hours wrap midnight`() {
        val routine = DailyRoutine(sleep = TimeOfDay.of(22), wakeUp = TimeOfDay.of(6))
        assertTrue(QuietHoursPolicy.isQuiet(TimeOfDay.of(23), routine))
        assertTrue(QuietHoursPolicy.isQuiet(TimeOfDay.of(5, 59), routine))
        assertFalse(QuietHoursPolicy.isQuiet(TimeOfDay.of(6), routine))
        assertFalse(QuietHoursPolicy.isQuiet(TimeOfDay.of(12), routine))
    }
}
