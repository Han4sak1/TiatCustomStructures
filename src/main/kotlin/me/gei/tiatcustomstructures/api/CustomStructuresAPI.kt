package me.gei.tiatcustomstructures.api

import me.gei.tiatcustomstructures.internal.dto.NearbyStructuresRequest
import me.gei.tiatcustomstructures.internal.dto.NearbyStructuresResponse
import me.gei.tiatcustomstructures.internal.managers.DatabaseManager
import org.bukkit.Location
import java.util.concurrent.CompletableFuture

object CustomStructuresAPI {
    /**
     * 获取附近建筑
     *
     * @param location 位置.
     * @param structureName 建筑名, "" 表示所有建筑.
     * @param limit 检索数量. 最大 20
     */
    fun getNearbyStructures(
        location: Location,
        structureName: String?,
        limit: Int
    ): CompletableFuture<NearbyStructuresResponse> {
        var newLimit = limit
        if (newLimit < 1 || newLimit > 20) newLimit = 1

        val nearbyStructuresRequest: NearbyStructuresRequest =
            if (structureName == null) {
            NearbyStructuresRequest(location, newLimit)
        } else {
            NearbyStructuresRequest(location, structureName, newLimit)
        }

        val response: CompletableFuture<NearbyStructuresResponse> = DatabaseManager.findNearby(nearbyStructuresRequest)

        return response
    }
}