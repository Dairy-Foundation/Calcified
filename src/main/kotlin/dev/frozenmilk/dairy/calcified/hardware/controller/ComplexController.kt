package dev.frozenmilk.dairy.calcified.hardware.controller

import dev.frozenmilk.dairy.calcified.Calcified
import dev.frozenmilk.dairy.calcified.hardware.controller.calculation.CalculationComponent
import dev.frozenmilk.dairy.calcified.hardware.motor.SimpleMotor
import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.FeatureRegistrar
import dev.frozenmilk.dairy.core.dependencyresolution.dependencies.Dependency
import dev.frozenmilk.dairy.core.dependencyresolution.dependencyset.DependencySet
import dev.frozenmilk.dairy.core.util.supplier.numeric.EnhancedNumberSupplier
import dev.frozenmilk.dairy.core.util.supplier.numeric.IEnhancedNumberSupplier
import dev.frozenmilk.dairy.core.util.supplier.numeric.MotionComponents
import dev.frozenmilk.dairy.core.wrapper.Wrapper
import java.util.function.Supplier

abstract class ComplexController<T: Comparable<T>>(
		var targetSupplier: Supplier<out T>,
		var motionComponent: MotionComponents,
		var toleranceEpsilon: T,
		val motors: SimpleMotor,
		val calculators: List<Pair<IEnhancedNumberSupplier<T>, CalculationComponent<T>>>,
		protected val indexedToUsrErr: Map<IEnhancedNumberSupplier<T>, Boolean>,
) : Feature {
	@Suppress("LeakingThis")
	override val dependencies: Set<Dependency<*, *>> = DependencySet(this)
			.yieldsTo(Calcified::class.java)

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
	var output: T = zero
		get() {
			if (!valid) {
				val currentTime = System.nanoTime()
				val deltaTime = (currentTime - previousTime) / 1e9
				field = zero
				val target = this.targetSupplier.get()
				calculators.forEach {
					val supplier = it.first
					val component = it.second
					field = component.calculate(field, supplier.position, target, supplier.componentError(motionComponent, target), deltaTime)
				}
				previousTime = currentTime
			}
			return field
		}
		private set
	val outputPower
		get() = toPower(output)

	fun update() {
		motors.power = outputPower
	}
	protected abstract fun toPower(output: T): Double

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

	init {
		@Suppress("LeakingThis")
		FeatureRegistrar.registerFeature(this)
	}

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
	 * an [EnhancedNumberSupplier] built off the output of this controller, useful for piping the output of this controller to another
	 */
	abstract val supplier: EnhancedNumberSupplier<T>
}
