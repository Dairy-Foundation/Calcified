package dev.frozenmilk.dairy.calcified.hardware.controller

import dev.frozenmilk.dairy.calcified.hardware.motor.MotorControllerGroup
import dev.frozenmilk.dairy.calcified.hardware.motor.SimpleMotor
import java.util.function.Supplier

class ControllerCompiler<IN> private constructor(
		val motors: SimpleMotor,
		val errorCalculators: Map<ErrorSupplier<in IN, Double>, ErrorController>,
		val positionCalculators: Map<Supplier<out IN>, PositionController<in IN>>,
		val lastErrorSupplier: ErrorSupplier<in IN, Double>?,
		val lastPositionSupplier: Supplier<out IN>?,
		// stores the error suppliers that are used to determine the user friendly error
		private val indexedToUsrErr: Map<ErrorSupplier<in IN, Double>, Boolean>
) {
	constructor() : this(MotorControllerGroup(), emptyMap(), emptyMap(), null, null, emptyMap())

	fun set(vararg motors: SimpleMotor) =
			ControllerCompiler(
					MotorControllerGroup(*motors),
					errorCalculators,
					positionCalculators,
					lastErrorSupplier,
					lastPositionSupplier,
					indexedToUsrErr
			)

	fun add(vararg motors: SimpleMotor) = set(this.motors, *motors)

	/**
	 * @param indexToError an error supplier that is NOT indexed to error will not be included in the results of [TODO] on the final compiled controller
	 */
	fun withErrorSupplier(errorSupplier: ErrorSupplier<IN, Double>, indexToError: Boolean = true) =
			ControllerCompiler(
					motors,
					errorCalculators,
					positionCalculators,
					errorSupplier,
					lastPositionSupplier,
					indexedToUsrErr + (errorSupplier to indexToError)
			)

	fun withPositionSupplier(positionSupplier: Supplier<out IN>) =
			ControllerCompiler(
					motors,
					errorCalculators,
					positionCalculators,
					lastErrorSupplier,
					positionSupplier,
					indexedToUsrErr
			)

	fun append(calculator: PositionController<IN>) =
			ControllerCompiler(
					motors,
					errorCalculators,
					positionCalculators + ((lastPositionSupplier ?: throw IllegalStateException("Position supplier cannot be inferred, try attaching one first using .with()")) to calculator),
					lastErrorSupplier,
					lastPositionSupplier,
					indexedToUsrErr
			)

	fun append(calculator: ErrorController) =
			ControllerCompiler(
					motors,
					errorCalculators + ((lastErrorSupplier ?: throw IllegalStateException("Error supplier cannot be inferred, try attaching one first using .with()")) to calculator),
					positionCalculators,
					lastErrorSupplier,
					lastPositionSupplier,
					indexedToUsrErr
			)

	fun compile(target: IN, toleranceEpsilon: Double) = ComplexController(target, toleranceEpsilon, motors, errorCalculators, positionCalculators, indexedToUsrErr)
}

