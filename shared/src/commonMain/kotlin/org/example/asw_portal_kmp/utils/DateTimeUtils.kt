package org.example.asw_portal_kmp.utils

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

object DateUtils {

    // Parse ISO 8601 format: "2026-06-06T02:48:41.398246"
    private fun parseDateTime(isoString: String): DateTimeComponents? {
        return try {
            // Split by 'T' to separate date and time
            val parts = isoString.split('T')
            if (parts.size != 2) return null

            val datePart = parts[0]
            val timePart = parts[1].split('.')[0] // Remove microseconds

            val dateComponents = datePart.split('-')
            if (dateComponents.size != 3) return null

            val timeComponents = timePart.split(':')
            if (timeComponents.size != 3) return null

            DateTimeComponents(
                year = dateComponents[0].toInt(),
                month = dateComponents[1].toInt(),
                day = dateComponents[2].toInt(),
                hour = timeComponents[0].toInt(),
                minute = timeComponents[1].toInt(),
                second = timeComponents[2].toInt()
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Format ISO 8601 to readable date time
     * "2026-06-06T02:48:41.398246" -> "Jun 6, 2026 2:48 AM"
     */
    fun formatDateTime(isoString: String): String {
        val components = parseDateTime(isoString) ?: return isoString

        val month = when (components.month) {
            1 -> "Jan"
            2 -> "Feb"
            3 -> "Mar"
            4 -> "Apr"
            5 -> "May"
            6 -> "Jun"
            7 -> "Jul"
            8 -> "Aug"
            9 -> "Sep"
            10 -> "Oct"
            11 -> "Nov"
            12 -> "Dec"
            else -> components.month.toString()
        }

        val hour = if (components.hour % 12 == 0) 12 else components.hour % 12
        val ampm = if (components.hour < 12) "AM" else "PM"

        return "$month ${components.day}, ${components.year} $hour:${components.minute.toString().padStart(2, '0')} $ampm"
    }

    /**
     * Format ISO 8601 to short date
     * "2026-06-06T02:48:41.398246" -> "06/06/2026"
     */
    fun formatDateShort(isoString: String): String {
        val components = parseDateTime(isoString) ?: return isoString
        return "${components.month.toString().padStart(2, '0')}/" +
                "${components.day.toString().padStart(2, '0')}/" +
                "${components.year}"
    }

    /**
     * Get time ago string
     * "2026-06-06T02:48:41.398246" -> "2 hours ago"
     */
    fun getTimeAgo(isoString: String): String {
        val components = parseDateTime(isoString) ?: return isoString

        // Create a timestamp for comparison (simplified)
        val now = getCurrentDateTime()

        val diffSeconds = calculateDifference(now, components)

        return when {
            diffSeconds < 60 -> "Just now"
            diffSeconds < 120 -> "1 minute ago"
            diffSeconds < 3600 -> "${diffSeconds / 60} minutes ago"
            diffSeconds < 7200 -> "1 hour ago"
            diffSeconds < 86400 -> "${diffSeconds / 3600} hours ago"
            diffSeconds < 172800 -> "Yesterday"
            diffSeconds < 604800 -> "${diffSeconds / 86400} days ago"
            diffSeconds < 2592000 -> "${diffSeconds / 604800} weeks ago"
            diffSeconds < 31536000 -> "${diffSeconds / 2592000} months ago"
            else -> "${diffSeconds / 31536000} years ago"
        }
    }

    /**
     * Relative time for display
     * "Updated 2 hours ago"
     */
    fun formatRelativeTime(isoString: String): String {
        val components = parseDateTime(isoString) ?: return "Updated $isoString"

        val now = getCurrentDateTime()
        val diffSeconds = calculateDifference(now, components)

        return when {
            diffSeconds < 60 -> "Updated just now"
            diffSeconds < 120 -> "Updated 1 minute ago"
            diffSeconds < 3600 -> "Updated ${diffSeconds / 60} minutes ago"
            diffSeconds < 7200 -> "Updated 1 hour ago"
            diffSeconds < 86400 -> "Updated ${diffSeconds / 3600} hours ago"
            diffSeconds < 172800 -> "Updated yesterday"
            else -> "Updated ${formatDateShort(isoString)}"
        }
    }

    // Helper data class
    private data class DateTimeComponents(
        val year: Int,
        val month: Int,
        val day: Int,
        val hour: Int,
        val minute: Int,
        val second: Int
    )

    private fun getCurrentDateTime(): DateTimeComponents {
        val currentMoment: Instant = kotlin.time.Clock.System.now()
        val datetimeInSystemZone: LocalDateTime = currentMoment.toLocalDateTime(TimeZone.currentSystemDefault())
        return DateTimeComponents(datetimeInSystemZone.year, datetimeInSystemZone.month.number, datetimeInSystemZone.day, datetimeInSystemZone.hour, datetimeInSystemZone.minute, datetimeInSystemZone.second)
    }

    // Calculate difference in seconds between two DateTimes
    private fun calculateDifference(now: DateTimeComponents, then: DateTimeComponents): Long {
        val nowLocal = LocalDateTime(now.year, now.month, now.day, now.hour, now.minute, now.second)
        val thenLocal = LocalDateTime(then.year, then.month, then.day, then.hour, then.minute, then.second)

        // Calculate difference in seconds
        val duration = nowLocal.toInstant(TimeZone.currentSystemDefault()) -
                thenLocal.toInstant(TimeZone.currentSystemDefault())

        return kotlin.math.abs(duration.inWholeSeconds)
    }
}