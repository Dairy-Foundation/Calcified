package dev.frozenmilk.dairy.calcified

import dev.frozenmilk.dairy.calcified.hardware.CalcifiedModule

abstract class CalcifiedDeviceMap<T> internal constructor(protected val module: CalcifiedModule, private val map: MutableMap<Byte, T> = mutableMapOf()) : MutableMap<Byte, T> by map

