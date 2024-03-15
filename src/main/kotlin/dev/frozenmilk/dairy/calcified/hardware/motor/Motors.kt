package dev.frozenmilk.dairy.calcified.hardware.motor

import com.qualcomm.robotcore.hardware.configuration.LynxConstants
import dev.frozenmilk.dairy.calcified.CalcifiedDeviceMap
import dev.frozenmilk.dairy.calcified.hardware.CalcifiedModule

class Motors internal constructor(module: CalcifiedModule) : CalcifiedDeviceMap<CalcifiedMotor>(module) {
	fun getMotor(port: Int): CalcifiedMotor {
		// checks to confirm that the motor port is validly in range
		if (port !in LynxConstants.INITIAL_MOTOR_PORT until LynxConstants.INITIAL_MOTOR_PORT + LynxConstants.NUMBER_OF_MOTORS) throw IllegalArgumentException("$port is not in the acceptable port range [${LynxConstants.INITIAL_MOTOR_PORT}, ${LynxConstants.INITIAL_MOTOR_PORT + LynxConstants.NUMBER_OF_MOTORS - 1}]")
		this.putIfAbsent(port, CalcifiedMotor(module, port))
		return this[port]!!
	}
}