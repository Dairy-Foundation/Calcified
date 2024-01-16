package dev.frozenmilk.dairy.calcified.hardware.encoder

import dev.frozenmilk.dairy.calcified.hardware.CalcifiedModule
import dev.frozenmilk.dairy.calcified.hardware.controller.BufferedCachedCompoundSupplier
import dev.frozenmilk.dairy.calcified.hardware.controller.CachedCompoundSupplier

class TicksEncoder internal constructor(module: CalcifiedModule, port: Byte) : CalcifiedEncoder<Int>(module, port) {
	override val positionSupplier: CachedCompoundSupplier<Int, Double> = object : CachedCompoundSupplier<Int, Double> {
		private var cachedPosition: Int? = null

		/**
		 * returns error in ticks, consider wrapping this encoder in a different UnitEncoder to use error with some other, more predictable unit
		 */
		override fun findError(target: Int): Double {
			return (target - get()).toDouble()
		}

		override fun clearCache() {
			cachedPosition = null
		}

		override fun get(): Int {
			if (cachedPosition == null) cachedPosition = module.bulkData.getEncoder(port.toInt()) * direction.multiplier
			return cachedPosition!! - offset
		}
	}

	override val velocitySupplier = object : BufferedCachedCompoundSupplier<Double, Double> {
		private var cachedVelocity: Double? = null
		private var cachedRawVelocity: Double? = null
		private var previousPositions = ArrayDeque(listOf(Pair(module.cachedTime, positionSupplier.get())))

		override fun findError(target: Double): Double {
			return target - get()
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

		override fun getRaw(): Double {
			get()
			return cachedRawVelocity!!
		}
	}
	override var offset: Int = 0
	override var position: Int
		get() {
			return positionSupplier.get()
		}
		set(value) {
			offset = value - positionSupplier.get()
		}
	override val cachedTime: Double
		get() {
			return module.cachedTime
		}
	override val previousCachedTime: Double
		get() {
			return module.previousCachedTime
		}

	var velocityTimeWindow = 0.2
}