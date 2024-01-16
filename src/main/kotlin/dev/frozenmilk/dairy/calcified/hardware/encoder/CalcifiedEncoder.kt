package dev.frozenmilk.dairy.calcified.hardware.encoder

import com.qualcomm.hardware.lynx.commands.core.LynxResetMotorEncoderCommand
import dev.frozenmilk.dairy.calcified.hardware.CalcifiedModule
import dev.frozenmilk.dairy.calcified.hardware.motor.Direction

abstract class CalcifiedEncoder<T> internal constructor(val module: CalcifiedModule, val port: Byte) : Encoder<T> {
	override var direction = Direction.FORWARD
	protected abstract var offset: T

	/**
	 * ensures that the velocity cache is cleared first, so it can store the previous position
	 */
	override fun clearCache() {
		velocitySupplier.clearCache()
		positionSupplier.clearCache()
	}

	override val velocity: Double
		get() {
			return velocitySupplier.get()
		}

	override fun reset() {
		LynxResetMotorEncoderCommand(module.lynxModule, port.toInt()).send()
	}
}

