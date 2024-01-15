package dev.frozenmilk.dairy.calcified.hardware.motor

import dev.frozenmilk.dairy.calcified.hardware.controller.CachedCompoundSupplier
import dev.frozenmilk.util.units.Angle
import dev.frozenmilk.util.units.AngleUnit

class AngleEncoder internal constructor(ticksEncoder: TicksEncoder,
										ticksPerWrap: Double,
										angleUnit: AngleUnit) : UnitEncoder<Angle>(ticksEncoder) {
	override var offset: Angle = position
	override var position: Angle
		get() { return positionSupplier.get() }
		set(value) { offset = value - positionSupplier.get() }


	override val positionSupplier = object : CachedCompoundSupplier<Angle, Double> {
		private var cachedAngle: Angle? = null
		override fun findError(target: Angle): Double {
			return get().into(angleUnit).findShortestDistance(target)
		}

		override fun clearCache() {
			cachedAngle = null
		}

		override fun get(): Angle {
			if (cachedAngle == null) cachedAngle = Angle(angleUnit, (ticksEncoder.positionSupplier.get().toDouble() * angleUnit.wrapAt / ticksPerWrap) * direction.multiplier)
			return cachedAngle!! - offset
		}
	}

	override val velocitySupplier = object : CachedCompoundSupplier<Double, Double> {
		private var cachedVelocity: Double? = null
		private var previousPosition = position

		override fun findError(target: Double): Double {
			return target - get()
		}

		override fun clearCache() {
			cachedVelocity = null
		}

		/**
		 * the velocity since the last time this method was called
		 */
		override fun get(): Double {
			if (cachedVelocity == null) {
				cachedVelocity = (position - previousPosition).value / (module.cachedTime - module.previousCachedTime)
				previousPosition = position
			}
			return cachedVelocity!!
		}
	}
}