package dev.frozenmilk.dairy.calcified.hardware.sensor

import com.qualcomm.robotcore.hardware.configuration.LynxConstants
import dev.frozenmilk.dairy.calcified.CalcifiedDeviceMap
import dev.frozenmilk.dairy.calcified.hardware.CalcifiedModule

class DigitalChannels internal constructor(module: CalcifiedModule) : CalcifiedDeviceMap<Any>(module) {
	fun getInput(port: Byte): DigitalInput {
		if (port !in 0 until LynxConstants.NUMBER_OF_DIGITAL_IOS) throw IllegalArgumentException("$port is not in the acceptable port range [0, ${LynxConstants.NUMBER_OF_DIGITAL_IOS - 1}]")
		if (this.containsKey(port) || this[port] !is DigitalInput) {
			this[port] = DigitalInput(module, port)
		}
		return (this[port] as DigitalInput)
	}
	fun getOutput(port: Byte): DigitalOutput {
		if (port !in 0 until LynxConstants.NUMBER_OF_DIGITAL_IOS) throw IllegalArgumentException("$port is not in the acceptable port range [0, ${LynxConstants.NUMBER_OF_DIGITAL_IOS - 1}]")
		if (this.containsKey(port) || this[port] !is DigitalOutput) {
			this[port] = DigitalInput(module, port)
		}
		return (this[port] as DigitalOutput)
	}
}