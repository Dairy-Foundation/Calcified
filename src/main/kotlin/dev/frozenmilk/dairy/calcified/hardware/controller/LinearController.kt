package dev.frozenmilk.dairy.calcified.hardware.controller

import dev.frozenmilk.dairy.calcified.hardware.motor.SimpleMotor
import java.util.function.Supplier
import kotlin.math.abs

class LinearController<IN>(target: IN, toleranceEpsilon: Double, motors: SimpleMotor, errorCalculators: Map<ErrorSupplier<in IN, out Double>, ErrorController<in Double>>, positionCalculators: Map<Supplier<out IN>, PositionController<in IN>>, indexedToUsrErr: Map<ErrorSupplier<in IN, out Double>, Boolean>) : ComplexController<IN, Double>(target, toleranceEpsilon, motors, errorCalculators, positionCalculators, indexedToUsrErr) {
	override fun error(target: IN) =
			errorIndexedCalculators
				.fold(0.0) { acc, errorSupplier ->
					acc + errorSupplier.findError(target)
				} / errorIndexedCalculators.size
	override fun finished(toleranceEpsilon: Double) = abs(error()) < toleranceEpsilon
}