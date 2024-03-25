package org.firstinspires.ftc.teamcode.boltbusterz.util

import dev.frozenmilk.dairy.calcified.hardware.controller.calculation.CalculationComponent
import dev.frozenmilk.util.units.ReifiedUnit
import dev.frozenmilk.util.units.Unit

class DoubleConstantFFComponent(var FF: Double) : CalculationComponent<Double> {
    override fun calculate(accumulation: Double, currentState: Double, target: Double, error: Double, deltaTime: Double) = accumulation + FF
}

/**
 * internal units are converted to common units using [ReifiedUnit.intoCommon]
 */
class UnitConstantFFComponent<U: Unit<U>, RU: ReifiedUnit<U, RU>>(var FF: RU) : CalculationComponent<RU> {
    override fun calculate(accumulation: RU, currentState: RU, target: RU, error: RU, deltaTime: Double) = accumulation + FF
}
}
