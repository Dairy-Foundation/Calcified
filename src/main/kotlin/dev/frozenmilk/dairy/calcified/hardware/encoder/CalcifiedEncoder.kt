package dev.frozenmilk.dairy.calcified.hardware.encoder

import com.qualcomm.hardware.lynx.commands.core.LynxResetMotorEncoderCommand
import dev.frozenmilk.dairy.calcified.hardware.CalcifiedModule
import dev.frozenmilk.dairy.calcified.hardware.motor.Direction

abstract class CalcifiedEncoder<T: Comparable<T>> internal constructor(val module: CalcifiedModule, val port: Int) : AbstractEncoder<T>() {
	override var direction = Direction.FORWARD
	protected abstract val zero: T
	override fun reset() {
		LynxResetMotorEncoderCommand(module.lynxModule, port.toInt()).send()
		position = zero
	}
}

