package dev.frozenmilk.dairy.calcified.hardware.encoder

import com.qualcomm.robotcore.hardware.DcMotor
import dev.frozenmilk.dairy.calcified.hardware.controller.BufferedCachedCompoundSupplier
import dev.frozenmilk.dairy.calcified.hardware.controller.CachedCompoundSupplier
import dev.frozenmilk.dairy.calcified.hardware.motor.Direction

class Wrapper(var motor: DcMotor) : Encoder<Int> {
	override var direction: Direction = Direction.FORWARD
	override val positionSupplier = object : CachedCompoundSupplier<Int, Double> {
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
			if (cachedPosition == null) cachedPosition = motor.currentPosition * direction.multiplier
			return cachedPosition!! - offset
		}
	}

	override val velocitySupplier = object : BufferedCachedCompoundSupplier<Double, Double> {

		private var cachedVelocity: Double? = null
		private var cachedRawVelocity: Double? = null
		private var previousPositions = ArrayDeque(listOf(Pair(cachedTime, positionSupplier.get())))

		override fun findError(target: Double): Double {
			return target - get()
		}

		override fun clearCache() {
			while (previousPositions.size >= 2 && cachedTime - previousPositions[1].first >= velocityTimeWindow) {
				previousPositions.removeFirst()
			}
			previousPositions.addLast(Pair(cachedTime, positionSupplier.get()))
			cachedVelocity = null
			cachedRawVelocity = null
			_previousCachedTime = cachedTime
			_cachedTime = System.nanoTime() / 1e9
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

		override fun getRaw(): Double {
			get()
			return cachedRawVelocity!!
		}
	}

	override var position: Int
		get() {
			return positionSupplier.get()
		}
		set(value) {
			offset = value - positionSupplier.get()
		}
	override val velocity: Double
		get() {
			return velocitySupplier.get()
		}
	private var _cachedTime: Double = System.nanoTime() / 1E9
	private var _previousCachedTime: Double = cachedTime
	override val cachedTime: Double
		get() {
			return _cachedTime
		}
	override val previousCachedTime: Double
		get() {
			return _previousCachedTime
		}

	override fun clearCache() {
		velocitySupplier.clearCache()
		positionSupplier.clearCache()
	}

	override fun reset() {
		val mode = motor.mode
		motor.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
		motor.mode = mode
	}

	private var offset: Int = 0
	var velocityTimeWindow = 0.2
}