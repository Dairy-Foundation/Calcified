package dev.frozenmilk.dairy.calcified.hardware.encoder

import dev.frozenmilk.dairy.calcified.hardware.motor.Direction
import dev.frozenmilk.dairy.core.util.supplier.numeric.EnhancedUnitSupplier
import dev.frozenmilk.util.units.distance.Distance
import dev.frozenmilk.util.units.distance.DistanceUnit

class DistanceEncoder(val encoder: Encoder<Double>, unit: DistanceUnit, ticksPerUnit: Double) : AbstractEncoder<Distance>() {
	override var direction: Direction
		get() = encoder.direction
		set(value) { encoder.direction = value }
	override val enhancedSupplier = EnhancedUnitSupplier({ Distance(unit, encoder.position / ticksPerUnit) })
	override fun reset() = encoder.reset()
}