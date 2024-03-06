package dev.frozenmilk.dairy.calcified.hardware.encoder

import dev.frozenmilk.dairy.calcified.hardware.motor.Direction
import dev.frozenmilk.dairy.core.util.supplier.logical.IConditional
import dev.frozenmilk.dairy.core.util.supplier.numeric.EnhancedComparableSupplier
import dev.frozenmilk.dairy.core.util.supplier.numeric.IEnhancedNumericSupplier

interface Encoder<T: Comparable<T>>: IEnhancedNumericSupplier<T>, EnhancedComparableSupplier<T, IConditional<T>> {
	var direction: Direction
	fun reset()
}