package dev.frozenmilk.dairy.calcified.hardware.controller

@FunctionalInterface
interface ErrorController<IN> {
	fun calculate(error: IN, deltaTime: Double): Double
}

class PController(var kP: Double) : ErrorController<Double> {
	override fun calculate(error: Double, deltaTime: Double) = error * kP
}

// todo bounding and such things, this is fine for a demo, someone else can do so quickly
class IController @JvmOverloads constructor(var kI: Double, var lowerLimit: Double = Double.NEGATIVE_INFINITY, var upperLimit: Double = Double.POSITIVE_INFINITY) : ErrorController<Double> {
	var i = 0.0
		private set
	override fun calculate(error: Double, deltaTime: Double): Double {
		i += error / deltaTime * kI
		i = i.coerceIn(lowerLimit, upperLimit)
		return i
	}
}

class DController(var kD: Double) : ErrorController<Double> {
	var previousError = 0.0
		private set
	override fun calculate(error: Double, deltaTime: Double): Double {
		val result = (error - previousError) / deltaTime * kD
		previousError = error
		return result
	}
}
