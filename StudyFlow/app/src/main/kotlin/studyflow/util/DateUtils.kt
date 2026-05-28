package studyflow.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateUtils {
    private val zone: ZoneId = ZoneId.systemDefault()
    private val shortFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH)
    private val fullFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)
    private val examFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm", Locale.ENGLISH)

    fun nowMillis(): Long = System.currentTimeMillis()

    fun today(): LocalDate = LocalDate.now(zone)

    fun todayStartMillis(): Long = today().atStartOfDay(zone).toInstant().toEpochMilli()

    fun dateToMillis(date: LocalDate): Long = date.atStartOfDay(zone).toInstant().toEpochMilli()

    fun dateTimeToMillis(dateTime: LocalDateTime): Long = dateTime.atZone(zone).toInstant().toEpochMilli()

    fun millisToDateTime(millis: Long): LocalDateTime = Instant.ofEpochMilli(millis).atZone(zone).toLocalDateTime()

    fun millisToDate(millis: Long): LocalDate = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()

    fun daysFromNow(days: Long): Long = dateToMillis(today().plusDays(days))

    fun formatShort(millis: Long?): String {
        if (millis == null) return "No deadline"
        return millisToDate(millis).format(shortFormatter)
    }

    fun formatFull(millis: Long?): String {
        if (millis == null) return "No deadline"
        return millisToDate(millis).format(fullFormatter)
    }

    fun formatIso(millis: Long?): String = millis?.let { millisToDate(it).toString() } ?: ""

    fun formatExamDateTime(millis: Long): String = millisToDateTime(millis).format(examFormatter)

    fun formatTimeRange(startAt: Long, endAt: Long): String {
        val start = millisToDateTime(startAt)
        val end = millisToDateTime(endAt)
        return "${start.toLocalDate()} ${start.toLocalTime()} — ${end.toLocalTime()}"
    }

    fun parseIsoDateToMillis(text: String): Long? {
        if (text.isBlank()) return null
        return runCatching { dateToMillis(LocalDate.parse(text.trim())) }.getOrNull()
    }

    fun isOverdue(millis: Long?): Boolean = millis != null && millis < todayStartMillis()

    fun isToday(millis: Long?): Boolean = millis != null && millisToDate(millis) == today()

    fun isThisWeek(millis: Long?): Boolean {
        if (millis == null) return false
        val date = millisToDate(millis)
        val start = today()
        val end = start.plusDays(7)
        return !date.isBefore(start) && date.isBefore(end)
    }

    fun weekStart(): LocalDate = today().minusDays((today().dayOfWeek.value - 1).toLong())

    fun monthGrid(month: YearMonth): List<LocalDate?> {
        val first = month.atDay(1)
        val leading = first.dayOfWeek.value - 1
        val result = mutableListOf<LocalDate?>()
        repeat(leading) { result += null }
        for (day in 1..month.lengthOfMonth()) result += month.atDay(day)
        while (result.size % 7 != 0) result += null
        return result
    }
}
