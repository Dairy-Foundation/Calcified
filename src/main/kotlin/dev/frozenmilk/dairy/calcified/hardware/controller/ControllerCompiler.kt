package dev.frozenmilk.dairy.calcified.hardware.controller

import dev.frozenmilk.dairy.calcified.hardware.motor.MotorControllerGroup
import dev.frozenmilk.dairy.calcified.hardware.motor.SimpleMotor
import java.util.function.Supplier

abstract class ControllerCompiler<IN, OUT> protected constructor(
		val motors: SimpleMotor,
		val errorCalculators: Map<ErrorSupplier<in IN, out OUT>, ErrorController<in OUT>>,
		val positionCalculators: Map<Supplier<out IN>, PositionController<in IN>>,
		val lastErrorSupplier: ErrorSupplier<in IN, out OUT>?,
		val lastPositionSupplier: Supplier<out IN>?,
		// stores the error suppliers that are used to determine the user friendly error
		protected val indexedToUsrErr: Map<ErrorSupplier<in IN, out OUT>, Boolean>
) {

	protected fun internalSet(motors: Array<out SimpleMotor>): SimpleMotor = MotorControllerGroup(*motors)
	abstract fun set(vararg motors: SimpleMotor): ControllerCompiler<IN, OUT>
	fun add(vararg motors: SimpleMotor) = set(this.motors, *motors)

	protected fun internalWithErrorSupplier(errorSupplier: ErrorSupplier<in IN, out OUT>, indexToError: Boolean) = indexedToUsrErr + (errorSupplier to indexToError)

	/**
	 * @param indexToError an error supplier that is NOT indexed to error will not be included in the results of the final compiled controller
	 */
	abstract fun withErrorSupplier(errorSupplier: ErrorSupplier<in IN, out OUT>, indexToError: Boolean = true): ControllerCompiler<IN, OUT>

	abstract fun withPositionSupplier(positionSupplier: Supplier<out IN>): ControllerCompiler<IN, OUT>

	protected fun internalAppend(calculator: ErrorController<in OUT>) =	errorCalculators + ((lastErrorSupplier ?: throw IllegalStateException("Error supplier cannot be inferred, try attaching one first using .with()")) to calculator)
	protected fun internalAppend(calculator: PositionController<in IN>) = positionCalculators + ((lastPositionSupplier ?: throw IllegalStateException("Position supplier cannot be inferred, try attaching one first using .with()")) to calculator)

	abstract fun append(calculator: PositionController<in IN>): ControllerCompiler<IN, OUT>

	abstract fun append(calculator: ErrorController<in OUT>): ControllerCompiler<IN, OUT>

	abstract fun compile(target: IN, toleranceEpsilon: Double): ComplexController<IN, OUT>
}
