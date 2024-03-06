package dev.frozenmilk.dairy.calcified.hardware.encoder

import dev.frozenmilk.dairy.core.util.supplier.logical.IConditional
import dev.frozenmilk.dairy.core.util.supplier.numeric.EnhancedComparableSupplier
import dev.frozenmilk.dairy.core.util.supplier.numeric.IEnhancedNumericSupplier
import dev.frozenmilk.dairy.core.util.supplier.numeric.MotionComponents
import dev.frozenmilk.util.modifier.Modifier
import dev.frozenmilk.dairy.core.wrapper.Wrapper
import java.util.function.Supplier

/**
 * used to automate the pass through process
 */
abstract class AbstractEncoder<T: Comparable<T>> : Encoder<T> {
	override val modifier
		get() = enhancedSupplier.modifier
	override val supplier
		get() = enhancedSupplier.supplier
	override var position
		get() = enhancedSupplier.position
		set(value) { enhancedSupplier.position = value }
	override val velocity
		get() = enhancedSupplier.velocity
	override val rawVelocity
		get() = enhancedSupplier.rawVelocity
	override val acceleration
		get() = enhancedSupplier.acceleration
	override val rawAcceleration
		get() = enhancedSupplier.rawAcceleration
	override var measurementWindow: Double
		get() = enhancedSupplier.measurementWindow
		set(value) { enhancedSupplier.measurementWindow = value }
	override val dependencies
		get() = enhancedSupplier.dependencies
	override var autoUpdates
		get() = enhancedSupplier.autoUpdates
		set(value) { enhancedSupplier.autoUpdates = value }
	abstract val enhancedSupplier: IEnhancedNumericSupplier<T>
	abstract val enhancedComparableSupplier: EnhancedComparableSupplier<T, IConditional<T>>
	override fun invalidate() = enhancedSupplier.invalidate()
	override fun findErrorPosition(target: T) = enhancedSupplier.findErrorPosition(target)
	override fun findErrorVelocity(target: T) = enhancedSupplier.findErrorVelocity(target)
	override fun findErrorRawVelocity(target: T) = enhancedSupplier.findErrorRawVelocity(target)
	override fun findErrorAcceleration(target: T) = enhancedSupplier.findErrorAcceleration(target)
	override fun findErrorRawAcceleration(target: T) = enhancedSupplier.findErrorRawAcceleration(target)
	override fun component(motionComponent: MotionComponents) = enhancedSupplier.component(motionComponent)
	override fun componentError(motionComponent: MotionComponents, target: T) = enhancedSupplier.componentError(motionComponent, target)
	override fun <T2> merge(supplier: Supplier<out T2>, merge: (T, T2) -> T) = enhancedSupplier.merge(supplier, merge)
	override fun applyModifier(modifier: Modifier<T>) = enhancedSupplier.applyModifier(modifier)
	override fun setModifier(modifier: Modifier<T>) = enhancedSupplier.setModifier(modifier)
	override fun conditionalBindPosition() = enhancedComparableSupplier.conditionalBindPosition()
	override fun conditionalBindVelocity() = enhancedComparableSupplier.conditionalBindVelocity()
	override fun conditionalBindVelocityRaw() = enhancedComparableSupplier.conditionalBindVelocityRaw()
	override fun conditionalBindAcceleration() = enhancedComparableSupplier.conditionalBindAcceleration()
	override fun conditionalBindAccelerationRaw() = enhancedComparableSupplier.conditionalBindAccelerationRaw()
	override fun preUserInitHook(opMode: Wrapper) = enhancedSupplier.preUserInitLoopHook(opMode)
	override fun preUserInitLoopHook(opMode: Wrapper) = enhancedSupplier.preUserInitLoopHook(opMode)
	override fun preUserStartHook(opMode: Wrapper) = enhancedSupplier.preUserStartHook(opMode)
	override fun preUserLoopHook(opMode: Wrapper) = enhancedSupplier.preUserLoopHook(opMode)
	override fun preUserStopHook(opMode: Wrapper) = enhancedSupplier.preUserStopHook(opMode)
	override fun postUserStopHook(opMode: Wrapper) = enhancedSupplier.postUserStopHook(opMode)
}