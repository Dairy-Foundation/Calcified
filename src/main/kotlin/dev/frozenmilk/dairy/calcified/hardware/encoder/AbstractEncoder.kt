package dev.frozenmilk.dairy.calcified.hardware.encoder

import dev.frozenmilk.dairy.core.util.supplier.numeric.IEnhancedNumberSupplier
import dev.frozenmilk.dairy.core.util.supplier.numeric.MotionComponents
import dev.frozenmilk.dairy.core.wrapper.Wrapper
import java.util.function.Supplier

/**
 * used to automate the pass through process
 */
abstract class AbstractEncoder<T: Comparable<T>> : Encoder<T> {
	override val modify
		get() = enhancedSupplier.modify
	override val lowerDeadzone
		get() = enhancedSupplier.lowerDeadzone
	override val upperDeadzone
		get() = enhancedSupplier.upperDeadzone
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
	abstract val enhancedSupplier: IEnhancedNumberSupplier<T>
	override fun invalidate() = enhancedSupplier.invalidate()
	override fun findErrorPosition(target: T) = enhancedSupplier.findErrorPosition(target)
	override fun findErrorVelocity(target: T) = enhancedSupplier.findErrorVelocity(target)
	override fun findErrorRawVelocity(target: T) = enhancedSupplier.findErrorRawVelocity(target)
	override fun findErrorAcceleration(target: T) = enhancedSupplier.findErrorAcceleration(target)
	override fun findErrorRawAcceleration(target: T) = enhancedSupplier.findErrorRawAcceleration(target)
	override fun component(motionComponent: MotionComponents) = enhancedSupplier.component(motionComponent)
	override fun componentError(motionComponent: MotionComponents, target: T) = enhancedSupplier.componentError(motionComponent, target)
	override fun <N2> merge(supplier: Supplier<out N2>, merge: (T, N2) -> T) = enhancedSupplier.merge(supplier, merge)
	override fun applyModifier(modify: (T) -> T) = enhancedSupplier.applyModifier(modify)
	override fun applyDeadzone(deadzone: T) = enhancedSupplier.applyDeadzone(deadzone)
	override fun applyDeadzone(lowerDeadzone: T, upperDeadzone: T) = enhancedSupplier.applyDeadzone(lowerDeadzone, upperDeadzone)
	override fun applyLowerDeadzone(lowerDeadzone: T) = enhancedSupplier.applyLowerDeadzone(lowerDeadzone)
	override fun applyUpperDeadzone(upperDeadzone: T) = enhancedSupplier.applyUpperDeadzone(upperDeadzone)
	override fun conditionalBindPosition() = enhancedSupplier.conditionalBindPosition()
	override fun conditionalBindVelocity() = enhancedSupplier.conditionalBindVelocity()
	override fun conditionalBindVelocityRaw() = enhancedSupplier.conditionalBindVelocityRaw()
	override fun conditionalBindAcceleration() = enhancedSupplier.conditionalBindAcceleration()
	override fun conditionalBindAccelerationRaw() = enhancedSupplier.conditionalBindAccelerationRaw()
	override fun preUserInitHook(opMode: Wrapper) = enhancedSupplier.preUserInitLoopHook(opMode)
	override fun preUserInitLoopHook(opMode: Wrapper) = enhancedSupplier.preUserInitLoopHook(opMode)
	override fun preUserStartHook(opMode: Wrapper) = enhancedSupplier.preUserStartHook(opMode)
	override fun preUserLoopHook(opMode: Wrapper) = enhancedSupplier.preUserLoopHook(opMode)
	override fun preUserStopHook(opMode: Wrapper) = enhancedSupplier.preUserStopHook(opMode)
	override fun postUserStopHook(opMode: Wrapper) = enhancedSupplier.postUserStopHook(opMode)
}