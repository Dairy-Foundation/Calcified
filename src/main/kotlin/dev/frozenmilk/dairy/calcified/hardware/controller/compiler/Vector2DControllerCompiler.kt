package dev.frozenmilk.dairy.calcified.hardware.controller.compiler

import dev.frozenmilk.dairy.calcified.hardware.controller.ControllerCompiler
import dev.frozenmilk.dairy.calcified.hardware.controller.calculation.CalculationComponent
import dev.frozenmilk.dairy.calcified.hardware.controller.implementation.Vector2DController
import dev.frozenmilk.dairy.core.util.supplier.numeric.IEnhancedNumericSupplier
import dev.frozenmilk.dairy.core.util.supplier.numeric.MotionComponents
import dev.frozenmilk.util.units.position.Vector2D
import java.util.function.Consumer
import java.util.function.Supplier

class Vector2DControllerCompiler private constructor(consumer: Consumer<Vector2D>, calculators: List<Pair<IEnhancedNumericSupplier<Vector2D>, CalculationComponent<Vector2D>>>, indexedToUsrErr: Map<IEnhancedNumericSupplier<Vector2D>, Boolean>, val currentSupplier: IEnhancedNumericSupplier<Vector2D>?) : ControllerCompiler<Vector2D>(consumer, calculators, indexedToUsrErr) {
	constructor() : this({}, emptyList(), emptyMap(), null)
	override fun set(vararg consumers: Consumer<Vector2D>) = Vector2DControllerCompiler({ p0 -> consumers.forEach { it.accept(p0) } }, calculators, indexedToUsrErr, currentSupplier)
	override fun withSupplier(enhancedSupplier: IEnhancedNumericSupplier<Vector2D>, indexedToUsrErr: Boolean) = Vector2DControllerCompiler(consumer, calculators, this.indexedToUsrErr.apply { this.plus(enhancedSupplier to indexedToUsrErr) }, enhancedSupplier)
	override fun withSupplier(enhancedSupplier: IEnhancedNumericSupplier<Vector2D>) = withSupplier(enhancedSupplier, true)
	override fun append(calculator: CalculationComponent<Vector2D>) = Vector2DControllerCompiler(consumer, calculators.plus(
			(currentSupplier ?: throw IllegalStateException("no supplier attached, an EnhancedSupplier of the appropriate type must be attached using 'withSupplier' before calculators can be attached"))
					to calculator
	), indexedToUsrErr, currentSupplier)
	override fun compile(target: Supplier<Vector2D>, motionComponent: MotionComponents, toleranceEpsilon: Vector2D) = Vector2DController(target, motionComponent, toleranceEpsilon, consumer, calculators, indexedToUsrErr)
}