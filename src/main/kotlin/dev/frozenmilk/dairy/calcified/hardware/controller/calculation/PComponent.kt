package dev.frozenmilk.dairy.calcified.hardware.controller.calculation

import dev.frozenmilk.util.units.ReifiedUnit
import dev.frozenmilk.util.units.Unit

class DoublePComponent(var kP: Double) : CalculationComponent<Double> {
	override fun calculate(accumulation: Double, position: Double, target: Double, error: Double, deltaTime: Double) = accumulation + (error * kP)
}
class UnitPComponent<U: Unit<U>, RU: ReifiedUnit<U, RU>>(var kP: Double) : CalculationComponent<RU> {
	override fun calculate(accumulation: RU, currentState: RU, target: RU, error: RU, deltaTime: Double) = accumulation + (error * kP)
}
