package dev.frozenmilk.dairy.calcified.hardware.controller

import dev.frozenmilk.dairy.calcified.hardware.controller.calculation.CalculationComponent
import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.dependency.lazy.Yielding
import dev.frozenmilk.dairy.core.util.supplier.numeric.EnhancedNumericSupplier
import dev.frozenmilk.dairy.core.util.supplier.numeric.IEnhancedNumericSupplier
import dev.frozenmilk.dairy.core.util.supplier.numeric.MotionComponents
import dev.frozenmilk.dairy.core.wrapper.Wrapper
import dev.frozenmilk.util.cell.LazyCell
import java.util.function.Consumer
import java.util.function.Supplier

/**
 * [deregister]s at the end of the OpMode
 */
abstract class ComplexController<T>(
		var targetSupplier: Supplier<out T>,
		var motionComponent: MotionComponents,
		var toleranceEpsilon: T,
		val outputConsumer: Consumer<T>,
		private val calculators: List<Pair<IEnhancedNumericSupplier<T>, CalculationComponent<T>>>,
		private val indexedToUsrErr: Map<IEnhancedNumericSupplier<T>, Boolean>,
) : Feature {
	override val dependency = Yielding
	init {
		@Suppress("LeakingThis")
		register()
	}

	private var previousTime = System.nanoTime()
	protected val errorIndexedSuppliers = calculators
			.map { it.first }
			.filter { indexedToUsrErr[it] ?: false }
	protected abstract val zero: T

	var target: T
		get() = targetSupplier.get()
		set(value) {
			targetSupplier = Supplier { value }
		}

	private var valid = false
	fun invalidate() {
		valid = false
	}
	/**
	 * the typed output of this controller, useful for piping it to another
	 */
	private var _output by LazyCell { zero }
	val output: T
		get() {
			if (!valid) {
				val currentTime = System.nanoTime()
				val deltaTime = (currentTime - previousTime) / 1e9
				_output = zero
				val target = this.targetSupplier.get()
				calculators.forEach {
					val supplier = it.first
					val component = it.second
					_output = component.calculate(_output, supplier.position, target, supplier.componentError(motionComponent, target), deltaTime)
				}
				previousTime = currentTime
			}
			return _output
		}

	fun update() {
		outputConsumer.accept(output)
	}

	/**
	 * average of all errors from the suppliers that are user indexed
	 */
	abstract fun error(target: Supplier<out T> = this.targetSupplier): Supplier<T>

	/**
	 * average of all errors from the suppliers that are user indexed
	 */
	fun error(target: T) = error( Supplier { target } ).get()

	/**
	 * @return if this controller has finished within variance of [toleranceEpsilon]
	 */
	abstract fun finished(toleranceEpsilon: T): Boolean

	/**
	 * [finished] but uses internal [toleranceEpsilon]
	 */
	fun finished() = finished(toleranceEpsilon)

	/**
	 * if this automatically updates, by calling [invalidate]
	 *
	 * this should be left true
	 *
	 * @see enabled
	 */
	var autoUpdates = true
	/**
	 * if this automatically updates, by calling [update]
	 *
	 * if this is false, the controller will not update motors automatically
	 *
	 * @see autoUpdates
	 */
	var enabled = true
	private fun autoUpdatePre() {
		if (autoUpdates) {
			update()
		}
	}
	private fun autoUpdatePost() {
		if (autoUpdates) {
			invalidate()
		}
	}

	override fun preUserInitHook(opMode: Wrapper) = autoUpdatePre()
	override fun postUserInitHook(opMode: Wrapper) = autoUpdatePost()
	override fun preUserInitLoopHook(opMode: Wrapper) = autoUpdatePre()
	override fun postUserInitLoopHook(opMode: Wrapper) = autoUpdatePost()
	override fun preUserStartHook(opMode: Wrapper) = autoUpdatePre()
	override fun postUserStartHook(opMode: Wrapper) = autoUpdatePost()
	override fun preUserLoopHook(opMode: Wrapper) = autoUpdatePre()
	override fun postUserLoopHook(opMode: Wrapper) = autoUpdatePost()
	override fun preUserStopHook(opMode: Wrapper) = autoUpdatePre()
	override fun postUserStopHook(opMode: Wrapper) {
		deregister()
	}

	/**
	 * an [EnhancedNumericSupplier] built off the output of this controller, useful for piping the output of this controller to another
	 */
	abstract val supplier: EnhancedNumericSupplier<T>
}
