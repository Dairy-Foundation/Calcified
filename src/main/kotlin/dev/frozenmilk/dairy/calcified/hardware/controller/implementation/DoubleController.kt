package dev.frozenmilk.dairy.calcified.hardware.controller.implementation

import dev.frozenmilk.dairy.calcified.hardware.controller.ComplexController
import dev.frozenmilk.dairy.calcified.hardware.controller.calculation.CalculationComponent
import dev.frozenmilk.dairy.calcified.hardware.motor.SimpleMotor
import dev.frozenmilk.dairy.core.util.supplier.numeric.EnhancedDoubleSupplier
import dev.frozenmilk.dairy.core.util.supplier.numeric.IEnhancedNumericSupplier
import dev.frozenmilk.dairy.core.util.supplier.numeric.MotionComponents
import java.util.function.Supplier
import kotlin.math.abs

class DoubleController(target: Supplier<out Double>, motionComponent: MotionComponents, toleranceEpsilon: Double, motors: SimpleMotor, calculators: List<Pair<IEnhancedNumericSupplier<Double>, CalculationComponent<Double>>>, indexedToUsrErr: Map<IEnhancedNumericSupplier<Double>, Boolean>) : ComplexController<Double>(target, motionComponent, toleranceEpsilon, motors, calculators, indexedToUsrErr) {
	override val zero = 0.0
	override val supplier by lazy { EnhancedDoubleSupplier(this::output) }
	override fun toPower(output: Double) = output
	override fun error(target: Supplier<out Double>) = Supplier {
		errorIndexedSuppliers
				.fold(zero) { acc, supplier ->
					acc + supplier.componentError(motionComponent, target.get())
				} / errorIndexedSuppliers.size.toDouble()
	}
	override fun finished(toleranceEpsilon: Double) = abs(error().get()) < toleranceEpsilon
}