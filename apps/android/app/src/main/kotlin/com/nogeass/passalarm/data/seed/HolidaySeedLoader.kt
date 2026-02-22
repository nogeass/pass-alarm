package com.nogeass.passalarm.data.seed

import android.content.Context
import com.nogeass.passalarm.domain.model.Holiday
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject
import javax.inject.Inject

class HolidaySeedLoader @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun loadBundledHolidays(): List<Holiday> {
        val fileNames = listOf(
            "holidays_jp_2025.json",
            "holidays_jp_2026.json",
            "holidays_jp_2027.json"
        )
        return fileNames.flatMap { fileName -> loadFile(fileName) }
    }

    private fun loadFile(fileName: String): List<Holiday> {
        val jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
        val root = JSONObject(jsonString)
        val holidays = root.getJSONArray("holidays")
        return (0 until holidays.length()).map { i ->
            val entry = holidays.getJSONObject(i)
            Holiday(
                date = entry.getString("date"),
                nameJa = entry.getString("name")
            )
        }
    }
}
