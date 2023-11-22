package com.github.ivc.expenses.db

import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Test
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class ZonedDateTimeConverterTest {
    private val conv = ZonedDateTimeConverter()
    private val epoch: Long = 1700000000 // Tue 14 Nov 2023 22:13:20 +00

    @After
    fun cleanup() {
        unmockkAll()
    }

    @Test
    fun marshal() {
        val mockedZoneDateTime = mockk<ZonedDateTime> {
            every { toEpochSecond() } returns epoch
        }
        val got = conv.marshal(mockedZoneDateTime)
        assertThat(got).isEqualTo(epoch)
    }

    @Test
    fun unmarshal() {
        val mockedZoneId = mockk<ZoneId>()
        val mockedZoneDateTime = mockk<ZonedDateTime>()
        val mockedInstant = mockk<Instant> {
            every { atZone(mockedZoneId) } returns mockedZoneDateTime
        }
        mockkStatic(Instant::class)
        mockkStatic(ZoneId::class)
        every { Instant.ofEpochSecond(epoch) } returns mockedInstant
        every { ZoneId.systemDefault() } returns mockedZoneId

        val got = conv.unmarshal(epoch)
        assertThat(got).isEqualTo(mockedZoneDateTime)
    }
}