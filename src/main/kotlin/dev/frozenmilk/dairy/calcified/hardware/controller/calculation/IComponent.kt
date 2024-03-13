package dev.frozenmilk.dairy.calcified.hardware.controller.calculation

import dev.frozenmilk.util.units.ReifiedUnit
import dev.frozenmilk.util.units.Unit

class DoubleIComponent @JvmOverloads constructor(var kI: Double, var lowerLimit: Double = Double.NEGATIVE_INFINITY, var upperLimit: Double = Double.POSITIVE_INFINITY) : CalculationComponent<Double> {
	var i = 0.0
	override fun calculate(accumulation: Double, currentState: Double, target: Double, error: Double, deltaTime: Double): Double {
		i += (error / deltaTime) * kI
		i = i.coerceIn(lowerLimit, upperLimit)
		return accumulation + i
	}
}

/**
 * internal units are converted to common units using [ReifiedUnit.intoCommon]
 */
class UnitIComponent<U: Unit<U>, RU: ReifiedUnit<U, RU>> @JvmOverloads constructor(var kI: Double, var lowerLimit: RU? = null, var upperLimit: RU? = null) : CalculationComponent<RU> {
	var i: RU? = null
	override fun calculate(accumulation: RU, currentState: RU, target: RU, error: RU, deltaTime: Double): RU {
		if (i == null) i = currentState - currentState
		i = i!! + (error.intoCommon() / deltaTime) * kI
		if (lowerLimit != null) i = i!!.coerceAtLeast(lowerLimit!!)
		if (upperLimit != null) i = i!!.coerceAtMost(upperLimit!!)
		return accumulation + i!!
	}
}