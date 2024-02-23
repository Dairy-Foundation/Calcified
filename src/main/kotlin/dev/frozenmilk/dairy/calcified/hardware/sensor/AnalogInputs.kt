package dev.frozenmilk.dairy.calcified.hardware.sensor

import com.qualcomm.robotcore.hardware.configuration.LynxConstants
import dev.frozenmilk.dairy.calcified.CalcifiedDeviceMap
import dev.frozenmilk.dairy.calcified.hardware.CalcifiedModule

class AnalogInputs internal constructor(module: CalcifiedModule) : CalcifiedDeviceMap<CalcifiedAnalogInput>(module) {
	fun getInput(port: Byte): CalcifiedAnalogInput {
		if (port !in 0 until LynxConstants.NUMBER_OF_ANALOG_INPUTS) throw IllegalArgumentException("$port is not in the acceptable port range [0, ${LynxConstants.NUMBER_OF_ANALOG_INPUTS - 1}]")
		this.putIfAbsent(port, CalcifiedAnalogInput(module, port))
		return this[port]!!
	}
}