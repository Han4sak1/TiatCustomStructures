package me.gei.tiatcustomstructures.internal.dto

import org.bukkit.Location

/**
 * 附近建筑查询申请
 */
class NearbyStructuresRequest(val location: Location, private val name: String, val limit: Int) {
    constructor(location: Location, limit: Int) : this(location, "", limit)

    fun getName(): String? {
        return name.ifEmpty { null }
    }

    fun hasName(): Boolean {
        return name.isNotEmpty()
    }
}
