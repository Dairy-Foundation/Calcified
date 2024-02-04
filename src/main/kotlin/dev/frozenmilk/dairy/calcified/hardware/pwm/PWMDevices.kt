package dev.frozenmilk.dairy.calcified.hardware.pwm

import com.qualcomm.robotcore.hardware.configuration.LynxConstants
import dev.frozenmilk.dairy.calcified.CalcifiedDeviceMap
import dev.frozenmilk.dairy.calcified.hardware.CalcifiedModule

class PWMDevices internal constructor(module: CalcifiedModule) : CalcifiedDeviceMap<PWMDevice>(module) {
	fun getServo(port: Byte): CalcifiedServo {
		if (port !in LynxConstants.INITIAL_SERVO_PORT until LynxConstants.INITIAL_SERVO_PORT + LynxConstants.NUMBER_OF_SERVO_CHANNELS) throw IllegalArgumentException("$port is not in the acceptable port range [${LynxConstants.INITIAL_SERVO_PORT}, ${LynxConstants.INITIAL_SERVO_PORT + LynxConstants.NUMBER_OF_SERVO_CHANNELS - 1}]")
		if (!this.containsKey(port) || this[port] !is CalcifiedServo) {
			this[port] = CalcifiedServo(module, port)
		}
		return (this[port] as CalcifiedServo)
	}

	fun getContinuousServo(port: Byte): CalcifiedContinuousServo {
		if (port !in LynxConstants.INITIAL_SERVO_PORT until LynxConstants.INITIAL_SERVO_PORT + LynxConstants.NUMBER_OF_SERVO_CHANNELS) throw IllegalArgumentException("$port is not in the acceptable port range [${LynxConstants.INITIAL_SERVO_PORT}, ${LynxConstants.INITIAL_SERVO_PORT + LynxConstants.NUMBER_OF_SERVO_CHANNELS - 1}]")
		if (!this.containsKey(port) || this[port] !is CalcifiedContinuousServo) {
			this[port] = CalcifiedContinuousServo(module, port)
		}
		return (this[port] as CalcifiedContinuousServo)
	}
}