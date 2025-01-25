package me.gei.tiatcustomstructures.internal.autoregister

import taboolib.common.io.runningClassesWithoutLibrary
import taboolib.library.reflex.ReflexClass
import taboolib.module.configuration.Configuration

object AutoRegisterFrame {

    private val propertyClasses: List<ReflexClass> by lazy {
        val classes = ArrayList<ReflexClass>()
        runningClassesWithoutLibrary.forEach { clazz ->
            if (clazz.hasAnnotation(StructureProperties::class.java))
                classes.add(clazz)
        }
        return@lazy classes
    }

    fun getPropertiesInstances(conf: Configuration): List<Any> {
        val inst = ArrayList<Any>()
        propertyClasses.forEach {
            it.newInstance(conf)!!.let { i -> inst.add(i) }
        }
        return inst
    }
}