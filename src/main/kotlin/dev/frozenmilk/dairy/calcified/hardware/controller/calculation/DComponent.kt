package dev.frozenmilk.dairy.calcified.hardware.controller.calculation

import dev.frozenmilk.util.units.ReifiedUnit
import dev.frozenmilk.util.units.Unit

class DoubleDComponent(var kD: Double) : CalculationComponent<Double> {
	private var previousError = 0.0

	override fun calculate(accumulation: Double, currentState: Double, target: Double, error: Double, deltaTime: Double): Double {
		val result = ((error - previousError) / deltaTime) * kD
		previousError = error
		return accumulation + result
	}
}

/**
 * internal units are converted to common units using [ReifiedUnit.intoCommon]
 */
class UnitDComponent<U: Unit<U>, RU: ReifiedUnit<U, RU>>(var kD: Double) : CalculationComponent<RU> {
	private var previousError: RU? = null

	override fun calculate(accumulation: RU, currentState: RU, target: RU, error: RU, deltaTime: Double): RU {
		if (previousError == null) previousError = currentState - currentState
		val result = ((error.intoCommon() - previousError!!) / deltaTime) * kD
		previousError = error
		return accumulation + result
	}
}
