package dev.frozenmilk.dairy.calcified.hardware.encoder

import dev.frozenmilk.dairy.calcified.hardware.CalcifiedModule
import dev.frozenmilk.dairy.core.util.supplier.numeric.EnhancedDoubleSupplier

class TicksEncoder internal constructor(module: CalcifiedModule, port: Byte) : CalcifiedEncoder<Double>(module, port) {
	override val enhancedSupplier = EnhancedDoubleSupplier({ module.bulkData.getEncoder(port.toInt()).toDouble() * direction.multiplier })
	override val enhancedComparableSupplier = enhancedSupplier
	override val zero = 0.0
}