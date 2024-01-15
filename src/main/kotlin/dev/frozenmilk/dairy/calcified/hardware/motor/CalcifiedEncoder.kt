package dev.frozenmilk.dairy.calcified.hardware.motor

import com.qualcomm.hardware.lynx.commands.core.LynxResetMotorEncoderCommand
import dev.frozenmilk.dairy.calcified.hardware.CalcifiedModule
import dev.frozenmilk.dairy.calcified.hardware.controller.CachedCompoundSupplier
import dev.frozenmilk.util.units.Unit

abstract class CalcifiedEncoder<T> internal constructor(val module: CalcifiedModule, val port: Byte) {
	var direction = Direction.FORWARD
	abstract val positionSupplier: CachedCompoundSupplier<T, Double>
	abstract val velocitySupplier: CachedCompoundSupplier<Double, Double>
	protected abstract var offset: T

	/**
	 * ensures that the velocity cache is cleared first, so it can store the previous position
	 */
	open fun clearCache() {
		velocitySupplier.clearCache()
		positionSupplier.clearCache()
	}

	abstract var position: T

	val velocity: Double
		get() {
			return velocitySupplier.get()
		}

	fun reset() {
		LynxResetMotorEncoderCommand(module.lynxModule, port.toInt()).send()
	}
}

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

	override val velocitySupplier: CachedCompoundSupplier<Double, Double> = object : CachedCompoundSupplier<Double, Double> {
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
				cachedVelocity = (position - previousPosition).toDouble() / (module.cachedTime - module.previousCachedTime)
				previousPosition = position
			}
			return cachedVelocity!!
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
}

abstract class UnitEncoder<T>(private val ticksEncoder: TicksEncoder) : CalcifiedEncoder<T>(ticksEncoder.module, ticksEncoder.port) {
	/**
	 * clears this cache first, then the ticks encoder cache, to ensure that we can grab previous values from the TicksEncoderCache
	 */
	override fun clearCache() {
		super.clearCache()
		ticksEncoder.clearCache()
	}
}

