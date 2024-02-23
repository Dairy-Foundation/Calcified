package dev.frozenmilk.dairy.calcified.hardware.encoder

import com.qualcomm.robotcore.hardware.DcMotor
import dev.frozenmilk.dairy.calcified.hardware.motor.Direction
import dev.frozenmilk.dairy.core.util.supplier.numeric.EnhancedDoubleSupplier

class Wrapper(val motor: DcMotor): AbstractEncoder<Double>() {
	override var direction: Direction = Direction.FORWARD
	override val enhancedSupplier = EnhancedDoubleSupplier({ motor.currentPosition.toDouble() * direction.multiplier })
	override fun reset() {
		val prev = motor.mode
		motor.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
		motor.mode = prev
	}
}