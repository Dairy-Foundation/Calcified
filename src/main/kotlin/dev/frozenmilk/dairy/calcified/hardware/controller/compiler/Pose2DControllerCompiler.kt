package dev.frozenmilk.dairy.calcified.hardware.controller.compiler

import dev.frozenmilk.dairy.calcified.hardware.controller.ControllerCompiler
import dev.frozenmilk.dairy.calcified.hardware.controller.calculation.CalculationComponent
import dev.frozenmilk.dairy.calcified.hardware.controller.implementation.Pose2DController
import dev.frozenmilk.dairy.core.util.supplier.numeric.IEnhancedNumericSupplier
import dev.frozenmilk.dairy.core.util.supplier.numeric.MotionComponents
import dev.frozenmilk.util.units.position.Pose2D
import java.util.function.Consumer
import java.util.function.Supplier

class Pose2DControllerCompiler private constructor(consumer: Consumer<Pose2D>, calculators: List<Pair<IEnhancedNumericSupplier<Pose2D>, CalculationComponent<Pose2D>>>, indexedToUsrErr: Map<IEnhancedNumericSupplier<Pose2D>, Boolean>, val currentSupplier: IEnhancedNumericSupplier<Pose2D>?) : ControllerCompiler<Pose2D>(consumer, calculators, indexedToUsrErr) {
	constructor() : this({}, emptyList(), emptyMap(), null)
	override fun set(vararg consumers: Consumer<Pose2D>) = Pose2DControllerCompiler({ p0 -> consumers.forEach { it.accept(p0) } }, calculators, indexedToUsrErr, currentSupplier)
	override fun withSupplier(enhancedSupplier: IEnhancedNumericSupplier<Pose2D>, indexedToUsrErr: Boolean) = Pose2DControllerCompiler(consumer, calculators, this.indexedToUsrErr.apply { this.plus(enhancedSupplier to indexedToUsrErr) }, enhancedSupplier)
	override fun withSupplier(enhancedSupplier: IEnhancedNumericSupplier<Pose2D>) = withSupplier(enhancedSupplier, true)
	override fun append(calculator: CalculationComponent<Pose2D>) = Pose2DControllerCompiler(consumer, calculators.plus(
			(currentSupplier ?: throw IllegalStateException("no supplier attached, an EnhancedSupplier of the appropriate type must be attached using 'withSupplier' before calculators can be attached"))
					to calculator
	), indexedToUsrErr, currentSupplier)
	override fun compile(target: Supplier<Pose2D>, motionComponent: MotionComponents, toleranceEpsilon: Pose2D) = Pose2DController(target, motionComponent, toleranceEpsilon, consumer, calculators, indexedToUsrErr)
}