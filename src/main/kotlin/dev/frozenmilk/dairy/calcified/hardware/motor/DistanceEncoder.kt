package dev.frozenmilk.dairy.calcified.hardware.motor

import dev.frozenmilk.dairy.calcified.hardware.controller.CachedCompoundSupplier
import dev.frozenmilk.util.units.Distance
import dev.frozenmilk.util.units.DistanceUnit

class DistanceEncoder(ticksEncoder: TicksEncoder,
					  ticksPerUnit: Double,
					  distanceUnit: DistanceUnit) : UnitEncoder<Distance>(ticksEncoder) {
	override var offset: Distance = position
	override var position: Distance
		get() { return positionSupplier.get() }
		set(value) { offset = value - positionSupplier.get() }


	override val positionSupplier = object : CachedCompoundSupplier<Distance, Double> {
		private var cachedDistance: Distance? = null
		override fun findError(target: Distance): Double {
			return (position - target).into(distanceUnit).value
		}

		override fun clearCache() {
			cachedDistance = null
		}

		override fun get(): Distance {
			if (cachedDistance == null) cachedDistance = Distance(distanceUnit, (ticksEncoder.positionSupplier.get().toDouble() / ticksPerUnit) * direction.multiplier)
			return cachedDistance!! - offset
		}
	}
	override val velocitySupplier = object : CachedCompoundSupplier<Double, Double> {
		private var cachedVelocity: Double? = null
		private var previousPosition = positionSupplier.get()

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