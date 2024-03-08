package dev.frozenmilk.dairy.calcified.hardware.encoder

import com.qualcomm.robotcore.hardware.configuration.LynxConstants
import dev.frozenmilk.dairy.calcified.CalcifiedDeviceMap
import dev.frozenmilk.dairy.calcified.hardware.CalcifiedModule
import dev.frozenmilk.util.units.angle.AngleUnit
import dev.frozenmilk.util.units.angle.Wrapping
import dev.frozenmilk.util.units.distance.DistanceUnit

class Encoders internal constructor(module: CalcifiedModule) : CalcifiedDeviceMap<Encoder<*>>(module) {

	/**
	 * if the port is empty, makes a new [TicksEncoder], else, overrides the encoder on the port
	 */
	fun getTicksEncoder(port: Byte): TicksEncoder {
		// this is pretty much the same as the motors, as the encoders match the motors
		// checks to confirm that the encoder port is validly in range
		if (port !in LynxConstants.INITIAL_MOTOR_PORT until LynxConstants.INITIAL_MOTOR_PORT + LynxConstants.NUMBER_OF_MOTORS) throw IllegalArgumentException("$port is not in the acceptable port range [${LynxConstants.INITIAL_MOTOR_PORT}, ${LynxConstants.INITIAL_MOTOR_PORT + LynxConstants.NUMBER_OF_MOTORS - 1}]")
		if (!contains(port) || this[port] !is TicksEncoder) {
			this[port] = TicksEncoder(module, port)
		}
		return (this[port] as TicksEncoder)
	}

	/**
	 * This method is useful for if you have your own [Encoder] overrides, for your own types, most of the time you want to use one of the other get<type>Encoder methods on this module
	 *
	 * @return Overrides the encoder on the port with a [Encoder] of the supplied type
	 */
	inline fun <reified T : Encoder<*>> getEncoder(lazySupplier: (TicksEncoder) -> T, port: Byte): T {
		val ticksEncoder = getTicksEncoder(port)
		this[port] = lazySupplier(ticksEncoder)
		return this[port] as? T ?: throw IllegalStateException("something went wrong while creating a new encoder, this shouldn't be reachable")
	}

	inline fun <reified T : Encoder<*>> getEncoder(port: Byte): T? {
		return this[port] as? T
	}

	/**
	 * overrides the encoder on the port with an [AngleEncoder], with the [ticksPerRevolution] and [wrapping] specified
	 */
	fun getAngleEncoder(port: Byte, wrapping: Wrapping, ticksPerRevolution: Double): AngleEncoder {
		return getEncoder({
			AngleEncoder(it, wrapping, ticksPerRevolution)
		}, port)
	}

	/**
	 * overrides the encoder on the port with a [DistanceEncoder], with the [ticksPerUnit] specified
	 */
	fun getDistanceEncoder(port: Byte, unit: DistanceUnit, ticksPerUnit: Double): DistanceEncoder {
		return getEncoder({
			DistanceEncoder(it, unit, ticksPerUnit)
		}, port)
	}
}