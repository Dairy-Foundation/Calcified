package dev.frozenmilk.dairy.calcified.hardware.controller.implementation

import dev.frozenmilk.dairy.calcified.hardware.controller.ComplexController
import dev.frozenmilk.dairy.calcified.hardware.controller.calculation.CalculationComponent
import dev.frozenmilk.dairy.core.util.supplier.numeric.EnhancedDoubleSupplier
import dev.frozenmilk.dairy.core.util.supplier.numeric.IEnhancedNumericSupplier
import dev.frozenmilk.dairy.core.util.supplier.numeric.MotionComponents
import java.util.function.Consumer
import java.util.function.Supplier
import kotlin.math.abs

class DoubleController(target: Supplier<out Double>, motionComponent: MotionComponents, toleranceEpsilon: Double, consumer: Consumer<Double>, calculators: List<Pair<IEnhancedNumericSupplier<Double>, CalculationComponent<Double>>>, indexedToUsrErr: Map<IEnhancedNumericSupplier<Double>, Boolean>) : ComplexController<Double>(target, motionComponent, toleranceEpsilon, consumer, calculators, indexedToUsrErr) {
	override val zero = 0.0
	override val supplier by lazy { EnhancedDoubleSupplier(this::output) }
	override fun error(target: Supplier<out Double>) = Supplier {
		val realisedTarget = target.get()
		errorIndexedSuppliers
				.fold(zero) { acc, supplier ->
					acc + supplier.componentError(motionComponent, realisedTarget)
				} / errorIndexedSuppliers.size.toDouble()
	}
	override fun finished(toleranceEpsilon: Double) = abs(error().get()) < toleranceEpsilon
}