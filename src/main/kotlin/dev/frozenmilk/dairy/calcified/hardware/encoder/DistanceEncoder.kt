package dev.frozenmilk.dairy.calcified.hardware.encoder

import dev.frozenmilk.dairy.calcified.hardware.controller.BufferedCachedCompoundSupplier
import dev.frozenmilk.dairy.calcified.hardware.controller.CachedCompoundSupplier
import dev.frozenmilk.dairy.calcified.hardware.motor.Direction
import dev.frozenmilk.util.units.distance.Distance
import dev.frozenmilk.util.units.distance.DistanceUnit

class DistanceEncoder(private val encoder: Encoder<Int>,
					  ticksPerUnit: Double,
					  distanceUnit: DistanceUnit) : Encoder<Distance> {
	private var offset: Distance = position
	override var position: Distance
		get() { return positionSupplier.get() }
		set(value) { offset = value - positionSupplier.get() }
	override val velocity: Double
		get() {
			return velocitySupplier.get()
		}
	override val cachedTime: Double
		get() {
			return encoder.cachedTime
		}
	override val previousCachedTime: Double
		get() {
			return encoder.previousCachedTime
		}

	override fun clearCache() {
		velocitySupplier.clearCache()
		positionSupplier.clearCache()
		encoder.clearCache()
	}

	override fun reset() {
		encoder.reset()
	}

	override var direction: Direction = Direction.FORWARD

	override val positionSupplier = object : CachedCompoundSupplier<Distance, Double> {
		private var cachedDistance: Distance? = null
		override fun findError(target: Distance): Double {
			return (position - target).into(distanceUnit).value
		}

		override fun clearCache() {
			cachedDistance = null
		}

		override fun get(): Distance {
			if (cachedDistance == null) cachedDistance = Distance(distanceUnit, (encoder.positionSupplier.get().toDouble() / ticksPerUnit) * direction.multiplier)
			return cachedDistance!! - offset
		}
	}
	override val velocitySupplier = object : BufferedCachedCompoundSupplier<Double, Double> {
		private var cachedVelocity: Double? = null
		private var cachedRawVelocity: Double? = null
		private var previousPositions = ArrayDeque(listOf(Pair(cachedTime, positionSupplier.get())))

		override fun findError(target: Double): Double {
			return target - get()
		}

		override fun getRaw(): Double {
			get()
			return cachedRawVelocity!!
		}

		override fun clearCache() {
			while (previousPositions.size >= 2 && encoder.cachedTime - previousPositions[1].first >= velocityTimeWindow) {
				previousPositions.removeFirst()
			}
			previousPositions.addLast(Pair(cachedTime, positionSupplier.get()))
			cachedVelocity = null
			cachedRawVelocity = null
		}

		/**
		 * the velocity since the last time this method was called
		 */
		override fun get(): Double {
			if (cachedVelocity == null) {
				while (previousPositions.size >= 2 && cachedTime - previousPositions[1].first >= velocityTimeWindow) {
					previousPositions.removeFirst()
				}
				cachedVelocity = (positionSupplier.get() - previousPositions[0].second).toDouble() / (cachedTime - previousPositions[0].first)
				cachedRawVelocity = (positionSupplier.get() - previousPositions.last().second).toDouble() / (cachedTime - previousPositions.last().first)
			}
			return cachedVelocity!!
		}
	}
	var velocityTimeWindow = 0.2
}