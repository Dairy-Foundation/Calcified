package dev.frozenmilk.dairy.calcified.hardware.encoder

import dev.frozenmilk.dairy.core.util.supplier.numeric.EnhancedUnitSupplier
import dev.frozenmilk.util.units.angle.Angle
import dev.frozenmilk.util.units.angle.AngleUnits
import dev.frozenmilk.util.units.angle.Wrapping

class AngleEncoder(val encoder: Encoder<Double>, wrapping: Wrapping = Wrapping.LINEAR, ticksPerRevolution: Double) : AbstractEncoder<Angle>() {
	override var direction
		get() = encoder.direction
		set(value) { encoder.direction = value }
	override val enhancedSupplier = EnhancedUnitSupplier({ Angle(AngleUnits.RADIAN, wrapping, (encoder.position / ticksPerRevolution) * Math.PI * 2 ) })
	override val enhancedComparableSupplier = enhancedSupplier
	override fun reset() = encoder.reset()
}