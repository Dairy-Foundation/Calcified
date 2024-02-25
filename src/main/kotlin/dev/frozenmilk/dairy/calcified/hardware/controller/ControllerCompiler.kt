package dev.frozenmilk.dairy.calcified.hardware.controller

import dev.frozenmilk.dairy.calcified.hardware.controller.calculation.CalculationComponent
import dev.frozenmilk.dairy.calcified.hardware.motor.MotorControllerGroup
import dev.frozenmilk.dairy.calcified.hardware.motor.SimpleMotor
import dev.frozenmilk.dairy.core.util.supplier.numeric.IEnhancedNumericSupplier
import dev.frozenmilk.dairy.core.util.supplier.numeric.MotionComponents
import java.util.function.Supplier

abstract class ControllerCompiler<T> protected constructor(
		val motors: SimpleMotor,
		val calculators: List<Pair<IEnhancedNumericSupplier<T>, CalculationComponent<T>>>,
		protected val indexedToUsrErr: Map<IEnhancedNumericSupplier<T>, Boolean>
) {
	protected fun internalSet(motors: Array<out SimpleMotor>): SimpleMotor = MotorControllerGroup(*motors)
	abstract fun set(vararg motors: SimpleMotor): ControllerCompiler<T>
	fun add(vararg motors: SimpleMotor) = set(this.motors, *motors)
	abstract fun withSupplier(enhancedSupplier: IEnhancedNumericSupplier<T>, vararg motionComponents: MotionComponents = MotionComponents.entries.toTypedArray()): ControllerCompiler<T>
	abstract fun append(calculator: CalculationComponent<T>): ControllerCompiler<T>
	abstract fun compile(target: Supplier<T>, motionComponent: MotionComponents, toleranceEpsilon: T): ComplexController<T>
	fun compile(target: T, motionComponent: MotionComponents, toleranceEpsilon: T) = compile({ target }, motionComponent, toleranceEpsilon)
}
