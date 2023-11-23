package com.github.ivc.expenses.db

import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class ZonedDateTimeConverterTest {
    private val mockedZonedDateTime: ZonedDateTime = mockk()
    private val mockedZoneId: ZoneId = mockk()
    private val mockedInstant: Instant = mockk {
        every { atZone(mockedZoneId) } returns mockedZonedDateTime
    }
    private val epoch: Long = 1700000000 // Tue 14 Nov 2023 22:13:20 +00
    
    private val conv = Converters(
        zoneId = mockedZoneId,
        currencyResolver = mockk(),
        instantResolver = { mockedInstant },
    )

    @Test
    fun zonedDateTimeToLong() {
        val mockedZoneDateTime = mockk<ZonedDateTime> {
            every { toEpochSecond() } returns epoch
        }
        val got = conv.zonedDateTimeToLong(mockedZoneDateTime)
        assertThat(got).isEqualTo(epoch)
    }

    @Test
    fun longToZonedDateTime() {
        val got = conv.longToZonedDateTime(epoch)
        assertThat(got).isEqualTo(mockedZonedDateTime)
    }
}