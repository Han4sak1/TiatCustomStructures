package me.gei.tiatcustomstructures.internal.dto

import org.bukkit.Location
import taboolib.module.database.Action
import java.sql.Connection
import java.sql.PreparedStatement

/**
 * 覆写原Action添加Location参数
 */
class ActionLocationQuery(val structure: String?, val location: Location, private val limit: Int) : Action {

    override val query: String
        get() = ""

    override val elements: List<Any>
        get() = if (structure == null) {
            listOf(location.x, location.y, location.z, location.world.name, limit)
        } else {
            listOf(location.x, location.y, location.z, structure, location.world.name, limit)
        }

    override fun callFinally(preparedStatement: PreparedStatement, connection: Connection) {}

    override fun onFinally(onFinally: PreparedStatement.(Connection) -> Unit) {}
}