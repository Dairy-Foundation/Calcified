package dev.frozenmilk.dairy.calcified.hardware.motor

import dev.frozenmilk.dairy.calcified.hardware.controller.BufferedCachedCompoundSupplier
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

	override val velocitySupplier = object : BufferedCachedCompoundSupplier<Double, Double> {
		private var cachedVelocity: Double? = null
		private var cachedRawVelocity: Double? = null
		private var previousPositions = ArrayDeque(listOf(Pair(module.cachedTime, positionSupplier.get())))

		override fun findError(target: Double): Double {
			return target - get()
		}

		override fun getRaw(): Double {
			get()
			return cachedRawVelocity!!
		}

		override fun clearCache() {
			while (previousPositions.size >= 2 && module.cachedTime - previousPositions[1].first >= velocityTimeWindow) {
				previousPositions.removeFirst()
			}
			previousPositions.addLast(Pair(module.cachedTime, positionSupplier.get()))
			cachedVelocity = null
			cachedRawVelocity = null
		}

		/**
		 * the velocity since the last time this method was called
		 */
		override fun get(): Double {
			if (cachedVelocity == null) {
				while (previousPositions.size >= 2 && module.cachedTime - previousPositions[1].first >= velocityTimeWindow) {
					previousPositions.removeFirst()
				}
				cachedVelocity = (positionSupplier.get() - previousPositions[0].second).toDouble() / (module.cachedTime - previousPositions[0].first)
				cachedRawVelocity = (positionSupplier.get() - previousPositions.last().second).toDouble() / (module.cachedTime - previousPositions.last().first)
			}
			return cachedVelocity!!
		}
	}

	var velocityTimeWindow = 0.2
}