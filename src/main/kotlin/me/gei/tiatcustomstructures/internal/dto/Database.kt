package me.gei.tiatcustomstructures.internal.dto

import me.gei.tiatcustomstructures.internal.managers.StructureManager
import org.bukkit.Bukkit
import org.bukkit.Location
import taboolib.common.platform.function.submitAsync
import java.util.concurrent.CompletableFuture
import javax.sql.DataSource

class Database(val type: Type, private val dataSource: DataSource = type.host().createDataSource()) {

    init {
        type.tableVar().createTable(dataSource)
    }

    fun add(structure: String, location: Location) {
        submitAsync {
            type.tableVar().insert(dataSource, "structure", "world", "x", "y", "z") {
                value(structure, location.world.name, location.x, location.y, location.z)
            }
        }
    }

    fun getNearest(request: NearbyStructuresRequest): CompletableFuture<NearbyStructuresResponse> {
        val result = CompletableFuture<NearbyStructuresResponse>()
        submitAsync {
            type.tableVar().transaction(dataSource) {
                if (request.hasName()) {
                    executeQuery("SELECT *, SQRT(POWER(x - ? , 2) + POWER(y - ? , 2) + POWER(z - ? , 2)) AS dist FROM ${table.name} WHERE structure = ? AND world = ? ORDER BY dist ASC LIMIT ?", ActionLocationQuery(request.getName(), request.location, request.limit))
                        .map {
                            NearbyStructuresResponse.NearbyStructureContainer(
                                Location(Bukkit.getWorld(getString("world")), getDouble("x"), getDouble("y"), getDouble("z")),
                                StructureManager.getStructure(request.getName()!!)!!,
                                getDouble("dist")
                            )
                        }.let { result.complete(NearbyStructuresResponse(it)) }
                } else {
                    executeQuery("SELECT *, SQRT(POWER(x - ? , 2) + POWER(y - ? , 2) + POWER(z - ? , 2)) AS dist FROM ${table.name} WHERE world = ? ORDER BY dist ASC LIMIT ?", ActionLocationQuery(null, request.location, request.limit))
                        .map {
                            NearbyStructuresResponse.NearbyStructureContainer(
                                Location(Bukkit.getWorld(getString("world")), getDouble("x"), getDouble("y"), getDouble("z")),
                                StructureManager.getStructure(getString("structure")),
                                getDouble("dist")
                            )
                        }
                        .let { result.complete(NearbyStructuresResponse(it)) }
                }
            }
        }
        return result
    }

    fun remove(structure: String) {
        submitAsync {
            type.tableVar().delete(dataSource) {
                where("structure" eq structure)
            }
        }
    }
}