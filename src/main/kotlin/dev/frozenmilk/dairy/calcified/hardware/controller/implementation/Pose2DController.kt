package dev.frozenmilk.dairy.calcified.hardware.controller.implementation

import EnhancedPose2DSupplier
import dev.frozenmilk.dairy.calcified.hardware.controller.ComplexController
import dev.frozenmilk.dairy.calcified.hardware.controller.calculation.CalculationComponent
import dev.frozenmilk.dairy.core.util.supplier.numeric.IEnhancedNumericSupplier
import dev.frozenmilk.dairy.core.util.supplier.numeric.MotionComponents
import dev.frozenmilk.util.units.position.Pose2D
import java.util.function.Consumer
import java.util.function.Supplier

class Pose2DController(targetSupplier: Supplier<out Pose2D>, motionComponent: MotionComponents, toleranceEpsilon: Pose2D, outputConsumer: Consumer<Pose2D>, calculators: List<Pair<IEnhancedNumericSupplier<Pose2D>, CalculationComponent<Pose2D>>>, indexedToUsrErr: Map<IEnhancedNumericSupplier<Pose2D>, Boolean>) : ComplexController<Pose2D>(targetSupplier, motionComponent, toleranceEpsilon, outputConsumer, calculators, indexedToUsrErr) {
	override val zero = Pose2D()
	override val supplier by lazy { EnhancedPose2DSupplier(this::output) }
	override fun error(target: Supplier<out Pose2D>) = Supplier {
		val realisedTarget = target.get()
		val sum = errorIndexedSuppliers
				.fold(zero) { acc, supplier ->
					acc.intoLinear() + supplier.componentError(motionComponent, realisedTarget)
				}
		val divisor = errorIndexedSuppliers.size.toDouble()
		Pose2D(sum.vector2D / divisor, sum.heading / divisor)
	}
	override fun finished(toleranceEpsilon: Pose2D): Boolean {
		val error = error().get()
		return error.vector2D.magnitude.abs() < toleranceEpsilon.vector2D.magnitude.abs() && error.heading.abs() < toleranceEpsilon.heading
	}
}