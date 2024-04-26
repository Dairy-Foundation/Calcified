package dev.frozenmilk.dairy.calcified.hardware.controller

import dev.frozenmilk.dairy.calcified.hardware.controller.calculation.CalculationComponent
import dev.frozenmilk.dairy.calcified.hardware.motor.MotorGroup
import dev.frozenmilk.dairy.calcified.hardware.motor.SimpleMotor
import dev.frozenmilk.dairy.core.util.supplier.numeric.IEnhancedNumericSupplier
import dev.frozenmilk.dairy.core.util.supplier.numeric.MotionComponents
import java.util.function.Consumer
import java.util.function.Supplier

abstract class ControllerCompiler<T> protected constructor(
		val consumer: Consumer<T>,
		val calculators: List<Pair<IEnhancedNumericSupplier<T>, CalculationComponent<T>>>,
		protected val indexedToUsrErr: Map<IEnhancedNumericSupplier<T>, Boolean>
) {
	protected fun internalSet(motors: Array<out SimpleMotor>): SimpleMotor = MotorGroup(*motors)
	abstract fun set(vararg consumers: Consumer<T>): ControllerCompiler<T>
	fun add(vararg consumers: Consumer<T>) = set(this.consumer, *consumers)
	abstract fun withSupplier(enhancedSupplier: IEnhancedNumericSupplier<T>, indexedToUsrErr: Boolean = true): ControllerCompiler<T>
	abstract fun append(calculator: CalculationComponent<T>): ControllerCompiler<T>
	abstract fun compile(target: Supplier<T>, motionComponent: MotionComponents, toleranceEpsilon: T): ComplexController<T>
	fun compile(target: T, motionComponent: MotionComponents, toleranceEpsilon: T) = compile({ target }, motionComponent, toleranceEpsilon)
}
