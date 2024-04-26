package dev.frozenmilk.dairy.calcified.hardware.controller.compiler

import dev.frozenmilk.dairy.calcified.hardware.controller.ControllerCompiler
import dev.frozenmilk.dairy.calcified.hardware.controller.calculation.CalculationComponent
import dev.frozenmilk.dairy.calcified.hardware.controller.implementation.DoubleController
import dev.frozenmilk.dairy.core.util.supplier.numeric.IEnhancedNumericSupplier
import dev.frozenmilk.dairy.core.util.supplier.numeric.MotionComponents
import java.util.function.Consumer
import java.util.function.Supplier

class DoubleControllerCompiler private constructor(consumer: Consumer<Double>, calculators: List<Pair<IEnhancedNumericSupplier<Double>, CalculationComponent<Double>>>, indexedToUsrErr: Map<IEnhancedNumericSupplier<Double>, Boolean>, val currentSupplier: IEnhancedNumericSupplier<Double>?) : ControllerCompiler<Double>(consumer, calculators, indexedToUsrErr) {
	constructor() : this({}, emptyList(), emptyMap(), null)
	override fun set(vararg consumers: Consumer<Double>) = DoubleControllerCompiler({ p0 -> consumers.forEach { it.accept(p0) } }, calculators, indexedToUsrErr, currentSupplier)
	override fun compile(target: Supplier<Double>, motionComponent: MotionComponents, toleranceEpsilon: Double) = DoubleController(target, motionComponent, toleranceEpsilon, consumer, calculators, indexedToUsrErr)
	override fun append(calculator: CalculationComponent<Double>) = DoubleControllerCompiler(consumer, calculators.plus(
			(currentSupplier ?: throw IllegalStateException("no supplier attached, an EnhancedSupplier of the appropriate type must be attached using 'withSupplier' before calculators can be attached"))
					to calculator
	), indexedToUsrErr, currentSupplier)
	override fun withSupplier(enhancedSupplier: IEnhancedNumericSupplier<Double>, indexedToUsrErr: Boolean) = DoubleControllerCompiler(consumer, calculators, this.indexedToUsrErr.apply { this.plus(enhancedSupplier to indexedToUsrErr) }, enhancedSupplier)
	override fun withSupplier(enhancedSupplier: IEnhancedNumericSupplier<Double>) = withSupplier(enhancedSupplier, true)
}