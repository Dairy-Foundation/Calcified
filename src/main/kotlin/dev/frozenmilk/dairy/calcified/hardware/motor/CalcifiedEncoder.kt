package dev.frozenmilk.dairy.calcified.hardware.motor

import com.qualcomm.hardware.lynx.commands.core.LynxResetMotorEncoderCommand
import dev.frozenmilk.dairy.calcified.hardware.CalcifiedModule
import dev.frozenmilk.dairy.calcified.hardware.controller.CachedCompoundSupplier
import dev.frozenmilk.util.angle.AngleDegrees
import dev.frozenmilk.util.angle.AngleRadians

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
    override val positionSupplier: CachedCompoundSupplier<Int, Double> = object :
        CachedCompoundSupplier<Int, Double> {
        private var cachedPosition: Int? = null
        private var cachedError: Double? = null

        /**
         * returns error in ticks, consider wrapping this encoder in a different UnitEncoder to use error with some other, more predictable unit
         */
        override fun findError(target: Int): Double {
            if (cachedError == null) cachedError = (target - get()).toDouble()
            return cachedError!!
        }

        override fun clearCache() {
            cachedError = null
            cachedPosition = null
        }

        override fun get(): Int {
            if (cachedPosition == null) cachedPosition = module.bulkData.getEncoder(port.toInt()) * direction.multiplier
            return cachedPosition!! - offset
        }
    }

    override val velocitySupplier: CachedCompoundSupplier<Double, Double> = object :
        CachedCompoundSupplier<Double, Double> {
        private var cachedVelocity: Double? = null
        private var cachedRawVelocity: Double? = null
        private var cachedError: Double? = null
        private var previousPositions = ArrayDeque(listOf(Pair(module.cachedTime, positionSupplier.get())))

        override fun findError(target: Double): Double {
            if (cachedError == null) cachedError = target - get()
            return cachedError!!
        }

        override fun clearCache() {
            while (previousPositions.size >= 2 && module.cachedTime - previousPositions[1].first >= velocityTimeWindow) {
                previousPositions.removeFirst()
            }
            previousPositions.addLast(Pair(module.cachedTime, positionSupplier.get()))
            cachedError = null
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

        /**
         * the unfiltered velocity since the last time this method was called
         */
        fun getRaw(): Double {
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
    var velocityTimeWindow: Double = 0.0
}

abstract class UnitEncoder<T>(private val ticksEncoder: TicksEncoder, protected val ticksPerUnit: Double) : CalcifiedEncoder<T>(ticksEncoder.module, ticksEncoder.port) {
	/**
	 * clears this cache first, then the ticks encoder cache, to ensure that we can grab previous values from the TicksEncoderCache
	 */
	override fun clearCache() {
		super.clearCache()
		ticksEncoder.clearCache()
	}
}

class RadiansEncoder internal constructor(ticksEncoder: TicksEncoder, ticksPerRevolution: Double) : UnitEncoder<AngleRadians>(ticksEncoder, ticksPerRevolution) {
	override val positionSupplier: CachedCompoundSupplier<AngleRadians, Double> = object : CachedCompoundSupplier<AngleRadians, Double> {
		private var cachedAngle: AngleRadians? = null
		private var cachedError: Double? = null

		override fun findError(target: AngleRadians): Double {
			if (cachedError == null) cachedError = get().findShortestDistance(target)
			return cachedError!!
		}

		override fun clearCache() {
			cachedAngle = null
			cachedError = null
		}

		override fun get(): AngleRadians {
			if (cachedAngle == null) cachedAngle = AngleRadians(Math.PI * 2 * (ticksEncoder.positionSupplier.get() / ticksPerRevolution) * direction.multiplier)
			return cachedAngle!! - offset
		}
	}

	override val velocitySupplier: CachedCompoundSupplier<Double, Double> = object : CachedCompoundSupplier<Double, Double> {
		private var cachedVelocity: Double? = null
		private var cachedError: Double? = null
		private var previousPosition = positionSupplier.get()

		override fun findError(target: Double): Double {
			if (cachedError == null) cachedError = target - get()
			return cachedError!!
		}

		override fun clearCache() {
			previousPosition = positionSupplier.get()
			cachedVelocity = null
			cachedError = null
		}

		override fun get(): Double {
			if (cachedVelocity == null) cachedVelocity = (positionSupplier.get() - previousPosition).theta / (module.cachedTime - module.previousCachedTime)
			return cachedVelocity!!
		}
	}
	override var offset: AngleRadians = AngleRadians()
	override var position: AngleRadians
		get() {
			return positionSupplier.get()
		}
		set(value) {
			offset = value - positionSupplier.get()
		}
}

class DegreesEncoder internal constructor(ticksEncoder: TicksEncoder, ticksPerRevolution: Double) : UnitEncoder<AngleDegrees>(ticksEncoder, ticksPerRevolution) {
	override val positionSupplier: CachedCompoundSupplier<AngleDegrees, Double> = object : CachedCompoundSupplier<AngleDegrees, Double> {
		private var cachedAngle: AngleDegrees? = null
		private var cachedError: Double? = null

		override fun findError(target: AngleDegrees): Double {
			if (cachedError == null) cachedError = get().findShortestDistance(target)
			return cachedError!!
		}

		override fun clearCache() {
			cachedAngle = null
			cachedError = null
		}

		override fun get(): AngleDegrees {
			if (cachedAngle == null) cachedAngle = AngleDegrees(360 * (ticksEncoder.positionSupplier.get() / ticksPerRevolution) * direction.multiplier)
			return cachedAngle!! - offset
		}
	}

	override val velocitySupplier: CachedCompoundSupplier<Double, Double> = object : CachedCompoundSupplier<Double, Double> {
		private var cachedVelocity: Double? = null
		private var cachedError: Double? = null
		private var previousPosition = positionSupplier.get()

		override fun findError(target: Double): Double {
			if (cachedError == null) cachedError = target - get()
			return cachedError!!
		}

		override fun clearCache() {
			previousPosition = positionSupplier.get()
			cachedVelocity = null
			cachedError = null
		}

		override fun get(): Double {
			if (cachedVelocity == null) cachedVelocity = (positionSupplier.get() - previousPosition).theta / (module.cachedTime - module.previousCachedTime)
			return cachedVelocity!!
		}
	}
	override var offset: AngleDegrees = AngleDegrees()
	override var position: AngleDegrees
		get() {
			return positionSupplier.get()
		}
		set(value) {
			offset = value - positionSupplier.get()
		}
}
