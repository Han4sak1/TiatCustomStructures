package me.gei.tiatcustomstructures.internal.dto

import taboolib.common.io.newFile
import taboolib.common.platform.function.pluginId
import taboolib.module.database.ColumnTypeSQLite
import taboolib.module.database.Host
import taboolib.module.database.Table
import taboolib.module.database.getHost
import java.io.File

class TypeSQLite(file: File, tableName: String? = null) : Type() {

    private val host = newFile(file).getHost()

    private val tableVar = Table(tableName ?: pluginId, host) {
        add("structure") {
            type(ColumnTypeSQLite.TEXT, 64)
        }
        add("world") {
            type(ColumnTypeSQLite.TEXT, 64)
        }
        add("x") {
            type(ColumnTypeSQLite.REAL)
        }
        add("y") {
            type(ColumnTypeSQLite.REAL)
        }
        add("z") {
            type(ColumnTypeSQLite.REAL)
        }
    }

    override fun host(): Host<*> {
        return host
    }

    override fun tableVar(): Table<*, *> {
        return tableVar
    }
}