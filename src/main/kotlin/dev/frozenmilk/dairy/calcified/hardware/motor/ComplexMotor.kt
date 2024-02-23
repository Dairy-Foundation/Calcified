package dev.frozenmilk.dairy.calcified.hardware.motor

import dev.frozenmilk.dairy.core.util.current.Current

interface ComplexMotor : SimpleMotor {
	var zeroPowerBehaviour: ZeroPowerBehaviour
	val current: Current
	var overCurrentThreshold: Current
	val overCurrent: Boolean
}