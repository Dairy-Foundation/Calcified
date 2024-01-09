package dev.frozenmilk.dairy.calcified.hardware.controller

import dev.frozenmilk.util.angle.Angle
import dev.frozenmilk.util.cell.LazyCell
import dev.frozenmilk.util.profile.AsymmetricMotionProfile
import dev.frozenmilk.util.profile.ProfileConstraints
import dev.frozenmilk.util.profile.ProfileStateComponent
import kotlin.math.cos

@FunctionalInterface
interface PositionController<IN> {
	fun calculate(position: IN, target: IN, deltaTime: Double): Double
}

class AngularFFController(var kF: Double) : PositionController<Angle> {
	override fun calculate(position: Angle, target: Angle, deltaTime: Double) = cos(position.intoRadians().theta) * kF
}

class MotionProfile<N : Number>(val constraints: ProfileConstraints, val component: ProfileStateComponent) : PositionController<N> {
	var profile: AsymmetricMotionProfile? = null
		private set

	var startTime = System.nanoTime()
		private set

	var previousTarget: N? = null
		private set
	override fun calculate(position: N, target: N, deltaTime: Double): Double {
		if(profile == null || target != previousTarget || target.toDouble() != profile!!.final) {
			profile = AsymmetricMotionProfile(position.toDouble(), target.toDouble(), constraints)
			startTime = System.nanoTime()
			previousTarget = target
		}
		return profile!!.calculate((System.nanoTime() - startTime) / 1E9, component)
	}
}