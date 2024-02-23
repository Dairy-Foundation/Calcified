package dev.frozenmilk.dairy.calcified.hardware.encoder

import dev.frozenmilk.dairy.calcified.hardware.motor.Direction
import dev.frozenmilk.dairy.core.util.supplier.numeric.IEnhancedNumberSupplier

interface Encoder<T: Comparable<T>>: IEnhancedNumberSupplier<T> {
	var direction: Direction
	fun reset()
}