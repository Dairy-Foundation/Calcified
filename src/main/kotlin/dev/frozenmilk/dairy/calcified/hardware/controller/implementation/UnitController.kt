package dev.frozenmilk.dairy.calcified.hardware.controller.implementation

import dev.frozenmilk.dairy.calcified.hardware.controller.ComplexController
import dev.frozenmilk.dairy.calcified.hardware.controller.calculation.CalculationComponent
import dev.frozenmilk.dairy.core.util.supplier.numeric.unit.EnhancedUnitSupplier
import dev.frozenmilk.dairy.core.util.supplier.numeric.IEnhancedNumericSupplier
import dev.frozenmilk.dairy.core.util.supplier.numeric.MotionComponents
import dev.frozenmilk.util.units.ReifiedUnit
import dev.frozenmilk.util.units.Unit
import java.util.function.Consumer
import java.util.function.Supplier

/**
 * internal units are converted to common units using [ReifiedUnit.intoCommon]
 */
class UnitController<U: Unit<U>, RU: ReifiedUnit<U, RU>>(target: Supplier<out RU>, motionComponent: MotionComponents, toleranceEpsilon: RU, consumer: Consumer<RU>, calculators: List<Pair<IEnhancedNumericSupplier<RU>, CalculationComponent<RU>>>, indexedToUsrErr: Map<IEnhancedNumericSupplier<RU>, Boolean>) : ComplexController<RU>({ target.get().intoCommon() }, motionComponent, toleranceEpsilon, consumer, calculators, indexedToUsrErr) {
	override val zero = target.get().run { this - this }.intoCommon()
	override val supplier by lazy { EnhancedUnitSupplier(this::output) }
	override fun error(target: Supplier<out RU>) = Supplier {
		val realisedTarget = target.get().intoCommon()
		errorIndexedSuppliers
				.fold(zero) { acc, supplier ->
					acc + supplier.componentError(motionComponent, realisedTarget)
				} / errorIndexedSuppliers.size.toDouble()
	}
	override fun finished(toleranceEpsilon: RU) = error().get().abs() < toleranceEpsilon
}