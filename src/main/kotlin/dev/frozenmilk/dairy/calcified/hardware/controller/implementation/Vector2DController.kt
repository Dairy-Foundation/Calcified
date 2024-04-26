package dev.frozenmilk.dairy.calcified.hardware.controller.implementation

import dev.frozenmilk.dairy.calcified.hardware.controller.ComplexController
import dev.frozenmilk.dairy.calcified.hardware.controller.calculation.CalculationComponent
import dev.frozenmilk.dairy.core.util.supplier.numeric.IEnhancedNumericSupplier
import dev.frozenmilk.dairy.core.util.supplier.numeric.MotionComponents
import dev.frozenmilk.dairy.core.util.supplier.numeric.positional.EnhancedVector2DSupplier
import dev.frozenmilk.util.units.position.Vector2D
import java.util.function.Consumer
import java.util.function.Supplier

class Vector2DController(targetSupplier: Supplier<out Vector2D>, motionComponent: MotionComponents, toleranceEpsilon: Vector2D, outputConsumer: Consumer<Vector2D>, calculators: List<Pair<IEnhancedNumericSupplier<Vector2D>, CalculationComponent<Vector2D>>>, indexedToUsrErr: Map<IEnhancedNumericSupplier<Vector2D>, Boolean>) : ComplexController<Vector2D>(targetSupplier, motionComponent, toleranceEpsilon, outputConsumer, calculators, indexedToUsrErr) {
	override val zero = Vector2D()
	override val supplier by lazy { EnhancedVector2DSupplier(this::output) }
	override fun error(target: Supplier<out Vector2D>) = Supplier {
		val realisedTarget = target.get()
		errorIndexedSuppliers
				.fold(zero) { acc, supplier ->
					acc + supplier.componentError(motionComponent, realisedTarget)
				} / errorIndexedSuppliers.size.toDouble()
	}
	override fun finished(toleranceEpsilon: Vector2D) = error().get().magnitude.abs() < toleranceEpsilon.magnitude.abs()
}