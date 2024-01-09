package dev.frozenmilk.dairy.calcified.hardware.controller

import dev.frozenmilk.dairy.calcified.Calcified
import dev.frozenmilk.dairy.calcified.hardware.motor.SimpleMotor
import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.FeatureRegistrar
import dev.frozenmilk.dairy.core.OpModeWrapper
import dev.frozenmilk.dairy.core.dependencyresolution.dependencies.Dependency
import dev.frozenmilk.dairy.core.dependencyresolution.dependencyset.DependencySet
import java.util.function.Supplier

abstract class ComplexController<IN, OUT> (var target: IN, var toleranceEpsilon: Double, val motors: SimpleMotor, val errorCalculators: Map<ErrorSupplier<in IN, out OUT>, ErrorController<in OUT>>, val positionCalculators: Map<Supplier<out IN>, PositionController<in IN>>, indexedToUsrErr: Map<ErrorSupplier<in IN, out OUT>, Boolean>) : Feature {
	override val dependencies: Set<Dependency<*, *>> = DependencySet(this)
			.yieldsTo(Calcified::class.java)

	protected var previousTime = System.nanoTime()

	protected val errorIndexedCalculators = errorCalculators
			.map { it.key }
			.filter { indexedToUsrErr[it] ?: false }

	fun update() {
		val currentTime = System.nanoTime()
		val deltaTime = (currentTime - previousTime) / 1e9
		var out = 0.0
		errorCalculators.forEach {
			out += it.value.calculate(it.key.findError(target), deltaTime)
		}
		positionCalculators.forEach {
			out += it.value.calculate(it.key.get(), target, deltaTime)
		}
		motors.power = out
		previousTime = currentTime
	}

	/**
	 * average of all errors from the suppliers that are user indexed
	 */
	abstract fun error(target: IN = this.target): OUT

	abstract fun finished(toleranceEpsilon: Double = this.toleranceEpsilon): Boolean

	init {
		FeatureRegistrar.registerFeature(this)
	}

	var enabled = true

	override fun postUserInitLoopHook(opMode: OpModeWrapper) {
		if (enabled) update()
	}

	override fun postUserLoopHook(opMode: OpModeWrapper) {
		if (enabled) update()
	}
}

