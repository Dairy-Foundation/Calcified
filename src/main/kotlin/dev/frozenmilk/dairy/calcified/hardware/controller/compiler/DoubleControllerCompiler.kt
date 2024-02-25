package dev.frozenmilk.dairy.calcified.hardware.controller.compiler

import dev.frozenmilk.dairy.calcified.hardware.controller.ControllerCompiler
import dev.frozenmilk.dairy.calcified.hardware.controller.calculation.CalculationComponent
import dev.frozenmilk.dairy.calcified.hardware.controller.implementation.DoubleController
import dev.frozenmilk.dairy.calcified.hardware.motor.MotorControllerGroup
import dev.frozenmilk.dairy.calcified.hardware.motor.SimpleMotor
import dev.frozenmilk.dairy.core.util.supplier.numeric.IEnhancedNumericSupplier
import dev.frozenmilk.dairy.core.util.supplier.numeric.MotionComponents
import java.util.function.Supplier

class DoubleControllerCompiler private constructor(motors: SimpleMotor, calculators: List<Pair<IEnhancedNumericSupplier<Double>, CalculationComponent<Double>>>, indexedToUsrErr: Map<IEnhancedNumericSupplier<Double>, Boolean>, val currentSupplier: IEnhancedNumericSupplier<Double>?) : ControllerCompiler<Double>(motors, calculators, indexedToUsrErr) {
	constructor() : this(MotorControllerGroup(), emptyList(), emptyMap(), null)
	override fun set(vararg motors: SimpleMotor) = DoubleControllerCompiler(MotorControllerGroup(*motors), calculators, indexedToUsrErr, currentSupplier)
	override fun compile(target: Supplier<Double>, motionComponent: MotionComponents, toleranceEpsilon: Double) = DoubleController(target, motionComponent, toleranceEpsilon, motors, calculators, indexedToUsrErr)
	override fun append(calculator: CalculationComponent<Double>) = DoubleControllerCompiler(motors, calculators.plus(
			(currentSupplier ?: throw IllegalStateException("no supplier attached, an EnhancedSupplier of the appropriate type must be attached using 'withSupplier' before calculators can be attached"))
					to calculator
	), indexedToUsrErr, currentSupplier)
	override fun withSupplier(enhancedSupplier: IEnhancedNumericSupplier<Double>, vararg motionComponents: MotionComponents) = DoubleControllerCompiler(motors, calculators, indexedToUsrErr, enhancedSupplier)
}