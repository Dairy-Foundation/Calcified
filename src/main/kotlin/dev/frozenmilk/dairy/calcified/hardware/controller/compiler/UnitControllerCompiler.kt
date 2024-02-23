package dev.frozenmilk.dairy.calcified.hardware.controller.compiler

import dev.frozenmilk.dairy.calcified.hardware.controller.ControllerCompiler
import dev.frozenmilk.dairy.calcified.hardware.controller.calculation.CalculationComponent
import dev.frozenmilk.dairy.calcified.hardware.controller.implementation.UnitController
import dev.frozenmilk.dairy.calcified.hardware.motor.MotorControllerGroup
import dev.frozenmilk.dairy.calcified.hardware.motor.SimpleMotor
import dev.frozenmilk.dairy.core.util.supplier.numeric.IEnhancedNumberSupplier
import dev.frozenmilk.dairy.core.util.supplier.numeric.MotionComponents
import dev.frozenmilk.util.units.ReifiedUnit
import dev.frozenmilk.util.units.Unit
import java.util.function.Supplier
class UnitControllerCompiler<U: Unit<U>, RU: ReifiedUnit<U, RU>> private constructor(motors: SimpleMotor, calculators: List<Pair<IEnhancedNumberSupplier<RU>, CalculationComponent<RU>>>, indexedToUsrErr: Map<IEnhancedNumberSupplier<RU>, Boolean>, val currentSupplier: IEnhancedNumberSupplier<RU>?) : ControllerCompiler<RU>(motors, calculators, indexedToUsrErr) {
	constructor(): this(MotorControllerGroup(), emptyList(), emptyMap(), null)
	override fun set(vararg motors: SimpleMotor) = UnitControllerCompiler(MotorControllerGroup(*motors), calculators, indexedToUsrErr, currentSupplier)
	override fun withSupplier(enhancedSupplier: IEnhancedNumberSupplier<RU>, vararg motionComponents: MotionComponents) = UnitControllerCompiler(motors, calculators, indexedToUsrErr, enhancedSupplier)
	override fun append(calculator: CalculationComponent<RU>) = UnitControllerCompiler(motors, calculators.plus(
			(currentSupplier ?: throw IllegalStateException("no supplier attached, an EnhancedSupplier of the appropriate type must be attached using 'withSupplier' before calculators can be attached"))
			to calculator
	), indexedToUsrErr, currentSupplier)
	override fun compile(target: Supplier<RU>, motionComponent: MotionComponents, toleranceEpsilon: RU) = UnitController(target, motionComponent, toleranceEpsilon, motors, calculators, indexedToUsrErr)
}