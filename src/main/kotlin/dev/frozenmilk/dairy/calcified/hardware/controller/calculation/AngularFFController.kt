package dev.frozenmilk.dairy.calcified.hardware.controller.calculation

import dev.frozenmilk.util.units.angle.Angle
import dev.frozenmilk.util.units.angle.AngleUnits
import dev.frozenmilk.util.units.angle.Wrapping

class AngularFFController(var kF: Double) : CalculationComponent<Angle> {
	override fun calculate(accumulation: Angle, currentState: Angle, target: Angle, error: Angle, deltaTime: Double) = accumulation.intoRadians().intoLinear() + Angle(AngleUnits.RADIAN, Wrapping.LINEAR, currentState.cos * kF)
}