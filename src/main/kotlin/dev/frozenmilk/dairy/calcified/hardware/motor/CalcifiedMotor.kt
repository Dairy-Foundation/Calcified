package dev.frozenmilk.dairy.calcified.hardware.motor

import com.qualcomm.hardware.lynx.commands.core.LynxGetADCCommand
import com.qualcomm.hardware.lynx.commands.core.LynxGetMotorChannelCurrentAlertLevelCommand
import com.qualcomm.hardware.lynx.commands.core.LynxSetMotorChannelCurrentAlertLevelCommand
import com.qualcomm.hardware.lynx.commands.core.LynxSetMotorChannelEnableCommand
import com.qualcomm.hardware.lynx.commands.core.LynxSetMotorChannelModeCommand
import com.qualcomm.hardware.lynx.commands.core.LynxSetMotorConstantPowerCommand
import com.qualcomm.robotcore.hardware.DcMotor
import dev.frozenmilk.dairy.calcified.hardware.CalcifiedModule
import dev.frozenmilk.dairy.core.util.current.Current
import dev.frozenmilk.dairy.core.util.current.CurrentUnits
import kotlin.math.abs
import kotlin.math.roundToInt

class CalcifiedMotor internal constructor(val module: CalcifiedModule, val port: Byte) : ComplexMotor {
	override var direction = Direction.FORWARD
	override var cachingTolerance = 0.005
	override var enabled = true
		set(value) {
			if (field != value) {
				// sends the command to change the enable state
				LynxSetMotorChannelEnableCommand(module.lynxModule, port.toInt(), value).send()
				field = value
			}
		}

	override var zeroPowerBehaviour = ZeroPowerBehaviour.FLOAT
		set(value) {
			if (field != value) {
				// sets the command to change the 0 power behaviour
				LynxSetMotorChannelModeCommand(module.lynxModule, port.toInt(), DcMotor.RunMode.RUN_WITHOUT_ENCODER, value.wrapping)
				field = value
			}
		}

	override var power = 0.0
		get() = if (enabled) field * direction.multiplier else 0.0
		set(value) {
			if (!enabled) return
			val correctedValue = value.coerceIn(-1.0, 1.0) * direction.multiplier
			if (abs(field - correctedValue) >= cachingTolerance || (correctedValue >= 1.0 && field != 1.0) || (correctedValue <= -1.0 && field != -1.0)) {
				LynxSetMotorConstantPowerCommand(module.lynxModule, port.toInt(), (correctedValue * LynxSetMotorConstantPowerCommand.apiPowerLast).toInt()).send()
				field = correctedValue
			}
		}

	override val current: Current
		get() {
			val milliAmps = LynxGetADCCommand(module.lynxModule, LynxGetADCCommand.Channel.motorCurrent(port.toInt()), LynxGetADCCommand.Mode.ENGINEERING).sendReceive().value
			return Current(CurrentUnits.MILLI_AMP, milliAmps.toDouble())
		}
	override var overCurrentThreshold = Current(CurrentUnits.MILLI_AMP, LynxGetMotorChannelCurrentAlertLevelCommand(module.lynxModule, port.toInt()).sendReceive().currentLimit.toDouble())
		set(value) {
			LynxSetMotorChannelCurrentAlertLevelCommand(module.lynxModule, port.toInt(), value.into(CurrentUnits.MILLI_AMP).value.roundToInt()).send()
			field = value
		}
	override val overCurrent: Boolean
		get() = module.bulkData.isOverCurrent(port.toInt())

	init {
		LynxSetMotorChannelEnableCommand(module.lynxModule, port.toInt(), true).send()
		LynxSetMotorChannelModeCommand(module.lynxModule, port.toInt(), DcMotor.RunMode.RUN_WITHOUT_ENCODER, ZeroPowerBehaviour.FLOAT.wrapping)
	}
}

