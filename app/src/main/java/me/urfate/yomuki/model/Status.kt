package me.urfate.yomuki.model

import me.urfate.yomuki.R

enum class Status(private val status: Int) {
    ONGOING(R.string.ongoing), COMPLETED(R.string.completed), UNKNOWN(R.string.unknown);

    fun toInt(): Int {
        return status
    }
}