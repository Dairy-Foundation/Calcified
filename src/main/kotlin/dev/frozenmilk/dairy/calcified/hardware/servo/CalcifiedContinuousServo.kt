package dev.frozenmilk.dairy.calcified.hardware.servo

import com.qualcomm.hardware.lynx.commands.core.LynxGetServoConfigurationCommand
import com.qualcomm.hardware.lynx.commands.core.LynxSetServoConfigurationCommand
import com.qualcomm.hardware.lynx.commands.core.LynxSetServoEnableCommand
import com.qualcomm.hardware.lynx.commands.core.LynxSetServoPulseWidthCommand
import com.qualcomm.robotcore.hardware.PwmControl
import com.qualcomm.robotcore.util.Range
import com.qualcomm.robotcore.util.RobotLog
import dev.frozenmilk.dairy.calcified.hardware.CalcifiedModule
import dev.frozenmilk.dairy.calcified.hardware.motor.Direction
import dev.frozenmilk.dairy.calcified.hardware.motor.SimpleMotor
import java.util.concurrent.CompletableFuture
import kotlin.math.abs

class CalcifiedContinuousServo internal constructor(val module: CalcifiedModule, val port: Byte) : SimpleMotor, PWMDevice {
	override var direction: Direction = Direction.FORWARD
	override var pwmRange: PwmControl.PwmRange = PwmControl.PwmRange.defaultRange
		set(value) {
			if (value.usFrame != field.usFrame) {
				LynxSetServoConfigurationCommand(module.lynxModule, port.toInt(), value.usFrame.toInt()).send()
			}
			field = value
		}

	override var cachingTolerance: Double = 0.005

	override var enabled: Boolean = false
		set(value) {
			firstEnable = true
			if (field != value) {
				// sends the command to change the enable state
				LynxSetServoEnableCommand(module.lynxModule, port.toInt(), value).send()
				field = value
			}
		}

	private var firstEnable = false
	override var power: Double = 0.0
		get() = if (enabled) field * direction.multiplier else 0.0
		set(value) {
			if (!enabled) {
				if (!firstEnable) enabled = true
				else return
			}
			val correctedValue = value.coerceIn(-1.0, 1.0) * direction.multiplier
			if (abs(field - correctedValue) >= cachingTolerance || (correctedValue >= 1.0 && field != 1.0) || (correctedValue <= -1.0 && field != -1.0)) {
				val pwm = Range
						.scale(correctedValue, -1.0, 1.0, pwmRange.usPulseLower, pwmRange.usPulseUpper)
						.toInt()
						.coerceIn(LynxSetServoPulseWidthCommand.apiPulseWidthFirst, LynxSetServoPulseWidthCommand.apiPulseWidthLast)

				LynxSetServoPulseWidthCommand(module.lynxModule, port.toInt(), pwm).send()
				field = correctedValue
			}
		}

	init {
		LynxSetServoConfigurationCommand(module.lynxModule, port.toInt(), pwmRange.usFrame.toInt()).send()
//		CompletableFuture.runAsync {
//			tryEnable()
//		}
	}
//
//	private fun tryEnable() {
//		try {
//			LynxSetServoEnableCommand(module.lynxModule, port.toInt(), true).send()
//		}
//		catch (_: Exception) {
//			tryEnable()
//		}
//	}
}
