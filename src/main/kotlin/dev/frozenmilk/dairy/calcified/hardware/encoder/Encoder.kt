package dev.frozenmilk.dairy.calcified.hardware.encoder

import dev.frozenmilk.dairy.calcified.hardware.motor.Direction
import dev.frozenmilk.dairy.core.util.supplier.logical.IConditional
import dev.frozenmilk.dairy.core.util.supplier.numeric.EnhancedComparableNumericSupplier

interface Encoder<T: Comparable<T>>: EnhancedComparableNumericSupplier<T, IConditional<T>> {
	var direction: Direction
	fun reset()
}