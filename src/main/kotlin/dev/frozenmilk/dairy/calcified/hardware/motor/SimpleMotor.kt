package dev.frozenmilk.dairy.calcified.hardware.motor

import java.util.function.Consumer

interface SimpleMotor : Consumer<Double> {
	var direction: Direction
	var cachingTolerance: Double
	var enabled: Boolean
	var power: Double
	// sets the power, ignoring the caching tolerance
	fun forcePower(power: Double) {
		val tolerance = cachingTolerance
		cachingTolerance = 0.0
		this.power = power
		cachingTolerance = tolerance
	}
	override fun accept(p0: Double) { power = p0 }
}