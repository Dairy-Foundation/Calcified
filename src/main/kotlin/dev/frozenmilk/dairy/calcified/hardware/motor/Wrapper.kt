package dev.frozenmilk.dairy.calcified.hardware.motor

import com.qualcomm.robotcore.hardware.DcMotorEx
import dev.frozenmilk.dairy.core.util.cachinghardwaredevice.CachingDcMotorSimple

class Wrapper(var motor: CachingDcMotorSimple) : SimpleMotor {
	override var direction: Direction
		get() {
			return Direction.from(motor.direction)
		}
		set(value) {
			motor.direction = value.into()
		}
	override var cachingTolerance: Double
		get() {
			return motor.cachingTolerance
		}
		set(value) {
			motor.cachingTolerance = value
		}
	override var enabled: Boolean
		get() {
			return (motor.dcMotorSimple as? DcMotorEx)?.isMotorEnabled ?: true
		}
		set(value) {
			if (value) {
				(motor.dcMotorSimple as? DcMotorEx)?.setMotorEnable()
			}
			else {
				(motor.dcMotorSimple as? DcMotorEx)?.setMotorDisable()
			}
		}
	override var power: Double
		get() {
			return motor.power
		}
		set(value) {
			motor.power = value
		}
}