package me.gei.tiatcustomstructures.internal.dto

import me.gei.tiatcustomstructures.internal.bo.structure.Structure
import org.bukkit.Location

/**
 * 附近建筑查询回应
 */
class NearbyStructuresResponse(val response: List<NearbyStructureContainer>) {
    fun hasEntries(): Boolean {
        return response.isNotEmpty()
    }

    class NearbyStructureContainer(
        val location: Location, private val structure: Structure?,
        val distance: Double
    ) {

        fun getStructure(): Structure? {
            return structure
        }
    }
}
