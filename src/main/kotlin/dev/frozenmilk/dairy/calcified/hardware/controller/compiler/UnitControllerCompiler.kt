package dev.frozenmilk.dairy.calcified.hardware.controller.compiler

import dev.frozenmilk.dairy.calcified.hardware.controller.ControllerCompiler
import dev.frozenmilk.dairy.calcified.hardware.controller.calculation.CalculationComponent
import dev.frozenmilk.dairy.calcified.hardware.controller.implementation.UnitController
import dev.frozenmilk.dairy.calcified.hardware.motor.SimpleMotor
import dev.frozenmilk.dairy.core.util.supplier.numeric.IEnhancedNumericSupplier
import dev.frozenmilk.dairy.core.util.supplier.numeric.MotionComponents
import dev.frozenmilk.util.units.ReifiedUnit
import dev.frozenmilk.util.units.Unit
import java.util.function.Consumer
import java.util.function.Supplier

class UnitControllerCompiler<U: Unit<U>, RU: ReifiedUnit<U, RU>> private constructor(consumer: Consumer<RU>, calculators: List<Pair<IEnhancedNumericSupplier<RU>, CalculationComponent<RU>>>, indexedToUsrErr: Map<IEnhancedNumericSupplier<RU>, Boolean>, val currentSupplier: IEnhancedNumericSupplier<RU>?) : ControllerCompiler<RU>(consumer, calculators, indexedToUsrErr) {
	constructor(): this({}, emptyList(), emptyMap(), null)
	fun set(vararg motors: SimpleMotor) = UnitControllerCompiler({ p0 -> motors.forEach { it.accept(p0.intoCommon().value) } }, calculators, indexedToUsrErr, currentSupplier)
	fun add(vararg motors: SimpleMotor) = set(this.consumer, { p0 -> motors.forEach { it.accept(p0.intoCommon().value) } })
	override fun set(vararg consumers: Consumer<RU>) = UnitControllerCompiler({ p0 -> consumers.forEach { it.accept(p0) } }, calculators, indexedToUsrErr, currentSupplier)
	override fun withSupplier(enhancedSupplier: IEnhancedNumericSupplier<RU>, indexedToUsrErr: Boolean) = UnitControllerCompiler(consumer, calculators, this.indexedToUsrErr.apply { this.plus(enhancedSupplier to indexedToUsrErr) }, enhancedSupplier)
	override fun withSupplier(enhancedSupplier: IEnhancedNumericSupplier<RU>) = withSupplier(enhancedSupplier, true)
	override fun append(calculator: CalculationComponent<RU>) = UnitControllerCompiler(consumer, calculators.plus(
			(currentSupplier ?: throw IllegalStateException("no supplier attached, an EnhancedSupplier of the appropriate type must be attached using 'withSupplier' before calculators can be attached"))
			to calculator
	), indexedToUsrErr, currentSupplier)
	override fun compile(target: Supplier<RU>, motionComponent: MotionComponents, toleranceEpsilon: RU) = UnitController(target, motionComponent, toleranceEpsilon, consumer, calculators, indexedToUsrErr)
}