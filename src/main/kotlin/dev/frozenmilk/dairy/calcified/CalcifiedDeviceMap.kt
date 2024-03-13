package dev.frozenmilk.dairy.calcified

import dev.frozenmilk.dairy.calcified.hardware.CalcifiedModule

abstract class CalcifiedDeviceMap<T> internal constructor(protected val module: CalcifiedModule, private val map: MutableMap<Int, T> = mutableMapOf()) : MutableMap<Int, T> by map

