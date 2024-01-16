package dev.frozenmilk.dairy.calcified.hardware.encoder

import dev.frozenmilk.dairy.calcified.hardware.controller.BufferedCachedCompoundSupplier
import dev.frozenmilk.dairy.calcified.hardware.controller.CachedCompoundSupplier
import dev.frozenmilk.dairy.calcified.hardware.motor.Direction

interface Encoder<T> {
	var direction: Direction
	val positionSupplier: CachedCompoundSupplier<T, Double>
	val velocitySupplier: BufferedCachedCompoundSupplier<Double, Double>
	var position: T
	val velocity: Double

	val cachedTime: Double
	val previousCachedTime: Double
	/**
	 * ensures that the velocity cache is cleared first, so it can store the previous position
	 */
	fun clearCache()
	fun reset()
}