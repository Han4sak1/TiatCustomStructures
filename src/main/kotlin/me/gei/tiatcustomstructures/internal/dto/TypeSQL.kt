package me.gei.tiatcustomstructures.internal.dto

import taboolib.module.database.*

class TypeSQL(private val host: Host<SQL>, table: String) : Type() {

    private val tableVar = Table(table, host) {
        add { id() }
        add("structure") {
            type(ColumnTypeSQL.VARCHAR, 64) {
                options(ColumnOptionSQL.KEY)
            }
        }
        add("world") {
            type(ColumnTypeSQL.VARCHAR, 64) {
                options(ColumnOptionSQL.KEY)
            }
        }
        add("x") {
            type(ColumnTypeSQL.DOUBLE)
        }
        add("y") {
            type(ColumnTypeSQL.DOUBLE)
        }
        add("z") {
            type(ColumnTypeSQL.DOUBLE)
        }
    }

    override fun host(): Host<*> {
        return host
    }

    override fun tableVar(): Table<*, *> {
        return tableVar
    }
}