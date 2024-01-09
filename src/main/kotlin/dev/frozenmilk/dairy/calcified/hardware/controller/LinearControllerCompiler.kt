package dev.frozenmilk.dairy.calcified.hardware.controller

import dev.frozenmilk.dairy.calcified.hardware.motor.MotorControllerGroup
import dev.frozenmilk.dairy.calcified.hardware.motor.SimpleMotor
import java.util.function.Supplier

class LinearControllerCompiler<IN> private constructor(
		motors: SimpleMotor,
		errorCalculators: Map<ErrorSupplier<in IN, out Double>, ErrorController<in Double>>,
		positionCalculators: Map<Supplier<out IN>, PositionController<in IN>>,
		lastErrorSupplier: ErrorSupplier<in IN, out Double>?,
		lastPositionSupplier: Supplier<out IN>?,
		// stores the error suppliers that are used to determine the user friendly error
		indexedToUsrErr: Map<ErrorSupplier<in IN, out Double>, Boolean>
) : ControllerCompiler<IN, Double>(
		motors,
		errorCalculators,
		positionCalculators,
		lastErrorSupplier,
		lastPositionSupplier,
		indexedToUsrErr
) {
	constructor() : this(MotorControllerGroup(), emptyMap(), emptyMap(), null, null, emptyMap())
	override fun set(vararg motors: SimpleMotor)  =
			LinearControllerCompiler(
					internalSet(motors),
					errorCalculators,
					positionCalculators,
					lastErrorSupplier,
					lastPositionSupplier,
					indexedToUsrErr
			)

	fun withErrorSupplier(errorSupplier: ErrorSupplier<in IN, out Double>)  =
			LinearControllerCompiler(
					motors,
					errorCalculators,
					positionCalculators,
					errorSupplier,
					lastPositionSupplier,
					internalWithErrorSupplier(errorSupplier, true)
			)

	override fun withErrorSupplier(errorSupplier: ErrorSupplier<in IN, out Double>, indexToError: Boolean)  =
			LinearControllerCompiler(
					motors,
					errorCalculators,
					positionCalculators,
					errorSupplier,
					lastPositionSupplier,
					internalWithErrorSupplier(errorSupplier, indexToError)
			)

	override fun withPositionSupplier(positionSupplier: Supplier<out IN>) =
			LinearControllerCompiler(
					motors,
					errorCalculators,
					positionCalculators,
					lastErrorSupplier,
					positionSupplier,
					indexedToUsrErr
			)

	override fun append(calculator: ErrorController<in Double>) =
			LinearControllerCompiler(
					motors,
					internalAppend(calculator),
					positionCalculators,
					lastErrorSupplier,
					lastPositionSupplier,
					indexedToUsrErr
			)

	override fun append(calculator: PositionController<in IN>) =
			LinearControllerCompiler(
					motors,
					errorCalculators,
					internalAppend(calculator),
					lastErrorSupplier,
					lastPositionSupplier,
					indexedToUsrErr
			)

	override fun compile(target: IN, toleranceEpsilon: Double) =
			LinearController(target, toleranceEpsilon, motors, errorCalculators, positionCalculators, indexedToUsrErr)
}