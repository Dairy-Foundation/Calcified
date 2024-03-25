package dev.frozenmilk.dairy.calcified.hardware.sensor

import com.qualcomm.robotcore.hardware.configuration.LynxConstants
import dev.frozenmilk.dairy.calcified.CalcifiedDeviceMap
import dev.frozenmilk.dairy.calcified.hardware.CalcifiedModule

class DigitalChannels internal constructor(module: CalcifiedModule) : CalcifiedDeviceMap<Any>(module) {
	fun getInput(port: Int): CalcifiedDigitalInput {
		if (port !in 0 until LynxConstants.NUMBER_OF_DIGITAL_IOS) throw IllegalArgumentException("$port is not in the acceptable port range [0, ${LynxConstants.NUMBER_OF_DIGITAL_IOS - 1}]")
		if (this.containsKey(port) || this[port] !is CalcifiedDigitalInput) {
			this[port] = CalcifiedDigitalInput(module, port)
		}
		return (this[port] as CalcifiedDigitalInput)
	}
	fun getOutput(port: Int): CalcifiedDigitalOutput {
		if (port !in 0 until LynxConstants.NUMBER_OF_DIGITAL_IOS) throw IllegalArgumentException("$port is not in the acceptable port range [0, ${LynxConstants.NUMBER_OF_DIGITAL_IOS - 1}]")
		if (this.containsKey(port) || this[port] !is CalcifiedDigitalOutput) {
			this[port] = CalcifiedDigitalInput(module, port)
		}
		return (this[port] as CalcifiedDigitalOutput)
	}
}