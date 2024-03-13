package dev.frozenmilk.dairy.calcified.hardware

import com.qualcomm.hardware.lynx.LynxModule
import com.qualcomm.hardware.lynx.commands.core.LynxGetBulkInputDataCommand
import com.qualcomm.hardware.lynx.commands.core.LynxGetBulkInputDataResponse
import com.qualcomm.robotcore.hardware.LynxModuleImuType
import dev.frozenmilk.dairy.calcified.hardware.sensor.AnalogInputs
import dev.frozenmilk.dairy.calcified.hardware.sensor.DigitalChannels
import dev.frozenmilk.dairy.calcified.hardware.encoder.Encoders
import dev.frozenmilk.dairy.calcified.hardware.sensor.I2CDevices
import dev.frozenmilk.dairy.calcified.hardware.motor.Motors
import dev.frozenmilk.dairy.calcified.hardware.pwm.PWMDevices
import dev.frozenmilk.dairy.calcified.hardware.encoder.AngleEncoder
import dev.frozenmilk.dairy.calcified.hardware.encoder.CalcifiedEncoder
import dev.frozenmilk.dairy.calcified.hardware.motor.CalcifiedMotor
import dev.frozenmilk.dairy.calcified.hardware.encoder.DistanceEncoder
import dev.frozenmilk.dairy.calcified.hardware.encoder.Encoder
import dev.frozenmilk.dairy.calcified.hardware.encoder.TicksEncoder
import dev.frozenmilk.dairy.calcified.hardware.sensor.CalcifiedAnalogInput
import dev.frozenmilk.dairy.calcified.hardware.sensor.CalcifiedIMU
import dev.frozenmilk.dairy.calcified.hardware.sensor.CalcifiedDigitalInput
import dev.frozenmilk.dairy.calcified.hardware.sensor.CalcifiedDigitalOutput
import dev.frozenmilk.dairy.calcified.hardware.pwm.CalcifiedContinuousServo
import dev.frozenmilk.dairy.calcified.hardware.pwm.CalcifiedServo
import dev.frozenmilk.util.cell.LateInitCell
import dev.frozenmilk.util.units.distance.DistanceUnit
import dev.frozenmilk.util.units.angle.Wrapping
import dev.frozenmilk.util.units.orientation.AngleBasedRobotOrientation

class CalcifiedModule(val lynxModule: LynxModule) {
	val motors = Motors(this)
	val encoders = Encoders(this)
	val PWMDevices = PWMDevices(this)
	val i2cDevices = I2CDevices(this)
	val digitalChannels = DigitalChannels(this)
	val analogInputs = AnalogInputs(this)
	var deviceMap: MutableMap<Class<*>, MutableMap<Int, out Any>> = mutableMapOf(
			CalcifiedMotor::class.java to motors,
			CalcifiedEncoder::class.java to encoders,
			CalcifiedServo::class.java to PWMDevices,
			CalcifiedIMU::class.java to i2cDevices,
			CalcifiedDigitalInput::class.java to digitalChannels,
			CalcifiedDigitalOutput::class.java to digitalChannels,
			CalcifiedAnalogInput::class.java to analogInputs,
	)
		private set

	fun <T> unsafeGet(type: Class<out T>, port: Int): T {
		val resultMap = deviceMap[type]
				?: throw IllegalArgumentException("no mappings of type ${type.simpleName} in this module's device mapping")
		val result = resultMap[port]
				?: throw IllegalArgumentException("no device of type ${type.simpleName} found at port $port")
		return type.cast(result)
				?: throw IllegalArgumentException("failed to cast device to type ${type.simpleName}")
	}

	var bulkData: LynxGetBulkInputDataResponse by LateInitCell()
		private set

	init {
		refreshBulkCache()
	}

	fun refreshBulkCache() {
		bulkData = LynxGetBulkInputDataCommand(lynxModule).sendReceive()
	}

	//
	// Remaps to the device maps
	//

	fun getMotor(port: Int): CalcifiedMotor {
		return motors.getMotor(port)
	}

	fun getTicksEncoder(port: Int): TicksEncoder {
		return encoders.getTicksEncoder(port)
	}

	fun getAttachedEncoder(port: Int) : Encoder<*>? {
		return encoders.getEncoder(port)
	}

	fun getDistanceEncoder(port: Int, unit: DistanceUnit, ticksPerUnit: Double): DistanceEncoder {
		return encoders.getDistanceEncoder(port, unit, ticksPerUnit)
	}

	fun getAngleEncoder(port: Int, wrapping: Wrapping, ticksPerRevolution: Double): AngleEncoder {
		return encoders.getAngleEncoder(port, wrapping, ticksPerRevolution)
	}

	fun getServo(port: Int): CalcifiedServo {
		return PWMDevices.getServo(port)
	}

	fun getContinuousServo(port: Int): CalcifiedContinuousServo {
		return PWMDevices.getContinuousServo(port)
	}

	@JvmOverloads
	fun getIMU_BHI260(port: Int = 0, angleBasedRobotOrientation: AngleBasedRobotOrientation = AngleBasedRobotOrientation()): CalcifiedIMU {
		return i2cDevices.getIMU_BHI260(port, angleBasedRobotOrientation)
	}

	@JvmOverloads
	fun getIMU_BNO055(port: Int = 0, angleBasedRobotOrientation: AngleBasedRobotOrientation = AngleBasedRobotOrientation()): CalcifiedIMU {
		return i2cDevices.getIMU_BNO055(port, angleBasedRobotOrientation)
	}

	@JvmOverloads
	fun getIMU(port: Int = 0, lynxModuleImuType: LynxModuleImuType = lynxModule.imuType, angleBasedRobotOrientation: AngleBasedRobotOrientation = AngleBasedRobotOrientation()): CalcifiedIMU {
		return i2cDevices.getIMU(port, lynxModuleImuType, angleBasedRobotOrientation)
	}

	fun getDigitalInput(port: Int): CalcifiedDigitalInput {
		return digitalChannels.getInput(port)
	}

	fun getDigitalOutput(port: Int): CalcifiedDigitalOutput {
		return digitalChannels.getOutput(port)
	}

	fun getAnalogInput(port: Int): CalcifiedAnalogInput {
		return analogInputs.getInput(port)
	}
}
