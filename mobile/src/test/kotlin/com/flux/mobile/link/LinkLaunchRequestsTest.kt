package com.flux.mobile.link

import android.app.Application
import android.content.Intent
import android.net.Uri
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class LinkLaunchRequestsTest {
    @Test
    fun `the watch link names the watch`() {
        assertEquals(LinkLaunch("a1b2-c3"), LinkLaunchRequests.parse(Uri.parse("flux://link?watch=a1b2-c3")))
    }

    @Test
    fun `without a watch id the user picks the watch`() {
        assertEquals(LinkLaunch(null), LinkLaunchRequests.parse(Uri.parse("flux://link")))
    }

    @Test
    fun `a malformed watch id is dropped`() {
        assertEquals(LinkLaunch(null), LinkLaunchRequests.parse(Uri.parse("flux://link?watch=%3Cscript%3E")))
    }

    @Test
    fun `other links are ignored`() {
        assertNull(LinkLaunchRequests.parse(Uri.parse("https://example.com/link?watch=x")))
        assertNull(LinkLaunchRequests.parse(Uri.parse("flux://other")))
        assertNull(LinkLaunchRequests.parse(null))
    }

    @Test
    fun `a request waits until it is consumed`() {
        val requests = LinkLaunchRequests()
        requests.handle(Intent(Intent.ACTION_VIEW, Uri.parse("flux://link?watch=w1")))
        assertEquals(LinkLaunch("w1"), requests.request.value)
        requests.consume()
        assertNull(requests.request.value)
    }
}
