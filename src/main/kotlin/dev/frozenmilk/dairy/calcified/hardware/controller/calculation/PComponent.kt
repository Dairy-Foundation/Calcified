package dev.frozenmilk.dairy.calcified.hardware.controller.calculation

import dev.frozenmilk.util.units.ReifiedUnit
import dev.frozenmilk.util.units.Unit

class DoublePComponent(var kP: Double) : CalculationComponent<Double> {
	override fun calculate(accumulation: Double, currentState: Double, target: Double, error: Double, deltaTime: Double) = accumulation + (error * kP)
}

/**
 * internal units are converted to common units using [ReifiedUnit.intoCommon]
 */
class UnitPComponent<U: Unit<U>, RU: ReifiedUnit<U, RU>>(var kP: Double) : CalculationComponent<RU> {
	override fun calculate(accumulation: RU, currentState: RU, target: RU, error: RU, deltaTime: Double) = accumulation + (error.intoCommon() * kP)
}
