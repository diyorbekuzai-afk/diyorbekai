package com.example.utils

object PriceCalculator {
    fun calculatePrice(elapsedMs: Long): Long {
        val totalMinutes = elapsedMs / 60000
        if (totalMinutes == 0L) return 0L
        
        return if (totalMinutes <= 30) {
            totalMinutes * 500
        } else if (totalMinutes <= 45) {
            15000L + (totalMinutes - 30) * 400
        } else if (totalMinutes <= 60) {
            21000L + (totalMinutes - 45) * 4000 / 15
        } else {
            val hours = totalMinutes / 60
            val remainingMins = totalMinutes % 60
            hours * 25000L + remainingMins * 416L
        }
    }
}
