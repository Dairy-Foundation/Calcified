package dev.frozenmilk.dairy.calcified.hardware.motor

import com.qualcomm.robotcore.hardware.DcMotorSimple

enum class Direction(val multiplier: Byte) {
	FORWARD(1),
	REVERSE(-1);

	fun into(): DcMotorSimple.Direction {
		return when (this) {
			FORWARD -> DcMotorSimple.Direction.FORWARD
			REVERSE -> DcMotorSimple.Direction.REVERSE
		}
	}
	companion object {
		@JvmStatic
		fun from(direction: DcMotorSimple.Direction): Direction {
			return when (direction) {
				DcMotorSimple.Direction.FORWARD -> FORWARD
				DcMotorSimple.Direction.REVERSE -> REVERSE
			}
		}
	}
}
