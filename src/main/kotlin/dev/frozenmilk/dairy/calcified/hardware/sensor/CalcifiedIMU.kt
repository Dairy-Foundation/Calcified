package dev.frozenmilk.dairy.calcified.hardware.sensor

import com.qualcomm.hardware.bosch.BHI260IMU
import com.qualcomm.hardware.bosch.BNO055IMU
import com.qualcomm.hardware.bosch.BNO055IMU.Register
import com.qualcomm.hardware.lynx.commands.core.LynxFirmwareVersionManager
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot
import com.qualcomm.robotcore.hardware.I2cAddr
import com.qualcomm.robotcore.hardware.ImuOrientationOnRobot
import com.qualcomm.robotcore.hardware.LynxModuleImuType
import com.qualcomm.robotcore.hardware.LynxModuleImuType.BHI260
import com.qualcomm.robotcore.hardware.LynxModuleImuType.BNO055
import com.qualcomm.robotcore.hardware.LynxModuleImuType.NONE
import com.qualcomm.robotcore.hardware.LynxModuleImuType.UNKNOWN
import com.qualcomm.robotcore.hardware.QuaternionBasedImuHelper.FailedToRetrieveQuaternionException
import com.qualcomm.robotcore.hardware.TimestampedData
import dev.frozenmilk.dairy.calcified.Calcified
import dev.frozenmilk.dairy.calcified.hardware.CalcifiedModule
import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.dependencyresolution.dependencies.Dependency
import dev.frozenmilk.dairy.core.dependencyresolution.dependencyset.DependencySet
import dev.frozenmilk.dairy.core.util.supplier.numeric.EnhancedUnitSupplier
import dev.frozenmilk.dairy.core.wrapper.Wrapper
import dev.frozenmilk.util.units.angle.Angle
import dev.frozenmilk.util.units.angle.AngleUnits
import dev.frozenmilk.util.units.angle.Wrapping
import dev.frozenmilk.util.units.orientation.AngleBasedRobotOrientation
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference
import org.firstinspires.ftc.robotcore.external.navigation.Orientation
import org.firstinspires.ftc.robotcore.external.navigation.Quaternion
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles
import org.firstinspires.ftc.robotcore.internal.hardware.android.AndroidBoard
import org.firstinspires.ftc.robotcore.internal.hardware.android.GpioPin
import org.firstinspires.ftc.robotcore.internal.system.AppUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder

class CalcifiedIMU internal constructor(val imuType: LynxModuleImuType, val module: CalcifiedModule, val port: Byte, initialAngles: AngleBasedRobotOrientation) : Feature {
	val device = LynxFirmwareVersionManager.createLynxI2cDeviceSynch(AppUtil.getDefContext(), module.lynxModule, port.toInt())
	init {
		when (imuType) {
			NONE, UNKNOWN -> throw IllegalStateException("Attempted to access IMU, but no accessible IMU found")
			BNO055 -> device.i2cAddr = BNO055IMU.I2CADDR_DEFAULT
			BHI260 -> device.i2cAddr = I2cAddr.create7bit(0x28)
		}
	}

	private var offsetOrientation = -initialAngles
	private var previousOrientation = initialAngles
	private var valid = false
	private var cachedTime = System.nanoTime()
	private var previousTime = cachedTime

	/**
	 * the current orientation of the robot
	 */
	var orientation: AngleBasedRobotOrientation = initialAngles
		get() {
			if (!valid) {
				previousOrientation = field
				// doesn't run through the setter function
				val result = readIMU()
				field = result.second - offsetOrientation
			}
			return field
		}
		set(value) {
			offsetOrientation = value - field
			field = value
		}

	/**
	 * the current orientation of the robot
	 */
	var yawPitchRollAngles: YawPitchRollAngles
		get() {
			return orientation.toYawPitchRoll()
		}
		set(value) {
			orientation = fromYawPitchRollAngles(value)
		}

	/**
	 * the heading of the robot, perfectly equivalent to the z axis of the robots rotation
	 */
	var heading: Angle
		get() {
			return orientation.zRot
		}
		set(value) {
			orientation = AngleBasedRobotOrientation(orientation.xRot, orientation.yRot, value)
		}

	/**
	 * a supplier that can be used in more complex applications of control loops
	 *
	 * supplies the robot's heading
	 */
	val headingSupplier = EnhancedUnitSupplier({ heading })

	/**
	 * same as [headingSupplier]
	 */
	val zRotSupplier = headingSupplier

	/**
	 * a supplier that can be used in more complex applications of control loops
	 *
	 * supplies the angle of the robot around the positive x-axis of the field
	 */
	val xRotSupplier = EnhancedUnitSupplier({ orientation.xRot })

	/**
	 * a supplier that can be used in more complex applications of control loops
	 *
	 * supplies the angle of the robot around the positive y-axis of the field
	 */
	val yRotSupplier = EnhancedUnitSupplier({ orientation.yRot })

	/**
	 * lets the imu know to perform a read next time it is queried
	 */
	fun invalidate() {
		valid = false
	}

	/**
	 * performs the actual read of the imu
	 */
	private fun readIMU(): Pair<Quaternion, AngleBasedRobotOrientation> {
		previousTime = cachedTime
		cachedTime = System.nanoTime()
		return when (imuType) {
			NONE, UNKNOWN -> throw IllegalStateException("Attempted to access IMU, but no accessible IMU found")

			BNO055 -> {
				val data: TimestampedData = device.readTimeStamped(Register.QUA_DATA_W_LSB.bVal.toInt(), 8)

				var receivedAllZeros = true
				for (b in data.data) {
					if (b != 0.toByte()) {
						receivedAllZeros = false
						break
					}
				}

				if (receivedAllZeros) {
					// All zeros is not a valid quaternion.
					throw FailedToRetrieveQuaternionException()
				}

				val buffer = ByteBuffer.wrap(data.data).order(ByteOrder.LITTLE_ENDIAN)
				val quaternion = Quaternion(buffer.getShort() / scale, buffer.getShort() / scale, buffer.getShort() / scale, buffer.getShort() / scale, data.nanoTime)
				quaternion to fromQuaternion(quaternion)
			}

			BHI260 -> {
				if (gameRVRequestGpio !is GpioPin) {
					// We must be running on a CH OS older than 1.1.3, there's no sense wasting time trying
					// to read a value.
					throw FailedToRetrieveQuaternionException()
				}

				gameRVRequestGpio.setState(true)
				val timestamp: Long = System.nanoTime()
				// We need to wait at least 500 microseconds before performing the I2C read. Fortunately
				// for us, that amount of time has already passed by the time that the internal LynxModule
				// finishes receiving the I2C read command.

				/**
				 * 0x32 is the target register, see [com.qualcomm.hardware.bosch.BHI260IMU.Register.GEN_PURPOSE_READ]
				 */
				val data: ByteBuffer = ByteBuffer.wrap(device.read(0x32, 8)).order(ByteOrder.LITTLE_ENDIAN)

				gameRVRequestGpio.setState(false)

				val xInt = data.short
				val yInt = data.short
				val zInt = data.short
				val wInt = data.short

				if (xInt == 0.toShort() && yInt == 0.toShort() && zInt == 0.toShort() && wInt == 0.toShort()) {
					// All zeros is not a valid quaternion.
					throw FailedToRetrieveQuaternionException()
				}

				val x = (xInt * QUATERNION_SCALE_FACTOR).toFloat()
				val y = (yInt * QUATERNION_SCALE_FACTOR).toFloat()
				val z = (zInt * QUATERNION_SCALE_FACTOR).toFloat()
				val w = (wInt * QUATERNION_SCALE_FACTOR).toFloat()
				val quaternion = Quaternion(w, x, y, z, timestamp)
				quaternion to fromQuaternion(quaternion)
			}
		}
	}

	//
	// Impl Feature
	//
	override val dependencies = DependencySet(this).dependsDirectlyOn(Calcified)

	/**
	 * if this automatically updates, by calling [invalidate] and then finding the [orientation]
	 */
	var autoUpdates = true
	private fun autoUpdate() {
		if (autoUpdates) {
			invalidate()
			orientation
		}
	}

	override fun preUserInitHook(opMode: Wrapper) = autoUpdate()
	override fun preUserInitLoopHook(opMode: Wrapper) = autoUpdate()
	override fun preUserStartHook(opMode: Wrapper) = autoUpdate()
	override fun preUserLoopHook(opMode: Wrapper) = autoUpdate()
	override fun preUserStopHook(opMode: Wrapper) = autoUpdate()
	override fun postUserStopHook(opMode: Wrapper) {
		deregister()
	}

	private companion object {
		/*
		constant values from the BHI260 IMU
		 */

		val QUATERNION_SCALE_FACTOR: Double = BHI260IMU::class.java.getDeclaredField("QUATERNION_SCALE_FACTOR").apply { this.isAccessible = true }.get(null) as Double

		// We want these fields to get initialized even if initialization ends up failing
		val gameRVRequestGpio = AndroidBoard.getInstance().bhi260Gpio5

		/*
		constant values from the BNO055 IMU
		 */

		const val scale: Float = (1 shl 14).toFloat()
	}
}

/**
 * makes an [AngleBasedRobotOrientation] from an [Orientation]
 */
fun fromOrientation(orientation: Orientation): AngleBasedRobotOrientation {
	val formattedOrientation = orientation
			.toAxesReference(AxesReference.EXTRINSIC)
			.toAxesOrder(AxesOrder.XYZ)
			.toAngleUnit(AngleUnit.RADIANS)
	return AngleBasedRobotOrientation(
			Angle(AngleUnits.RADIAN, Wrapping.WRAPPING, formattedOrientation.firstAngle.toDouble()),
			Angle(AngleUnits.RADIAN, Wrapping.WRAPPING, formattedOrientation.secondAngle.toDouble()),
			Angle(AngleUnits.RADIAN, Wrapping.WRAPPING, formattedOrientation.thirdAngle.toDouble())
	)
}

/**
 * makes an [AngleBasedRobotOrientation] from an [ImuOrientationOnRobot]
 *
 * useful when defining an imu from a [RevHubOrientationOnRobot]
 */
fun fromImuOrientationOnRobot(imuOrientationOnRobot: ImuOrientationOnRobot): AngleBasedRobotOrientation {
	return fromQuaternion(imuOrientationOnRobot.imuCoordinateSystemOrientationFromPerspectiveOfRobot())
}

/**
 * makes an [AngleBasedRobotOrientation] from a [Quaternion]
 */
fun fromQuaternion(quaternion: Quaternion): AngleBasedRobotOrientation {
	// this probably sucks and is slow but eh, can't really be that bad, and saves me from quaternion hell
	return fromOrientation(quaternion.toOrientation(AxesReference.EXTRINSIC, AxesOrder.XYZ, AngleUnit.RADIANS))
}

fun fromYawPitchRollAngles(yawPitchRollAngles: YawPitchRollAngles): AngleBasedRobotOrientation {
	return fromOrientation(Orientation(AxesReference.INTRINSIC, AxesOrder.ZXY, AngleUnit.DEGREES, yawPitchRollAngles.getYaw(AngleUnit.DEGREES).toFloat(), yawPitchRollAngles.getPitch(AngleUnit.DEGREES).toFloat(), yawPitchRollAngles.getRoll(AngleUnit.DEGREES).toFloat(), yawPitchRollAngles.acquisitionTime))
}

fun AngleBasedRobotOrientation.toOrientation(): Orientation {
	return Orientation(AxesReference.EXTRINSIC, AxesOrder.XYZ, AngleUnit.RADIANS, xRot.intoRadians().value.toFloat(), yRot.intoRadians().value.toFloat(), zRot.intoRadians().value.toFloat(), 0L)
}

fun AngleBasedRobotOrientation.toYawPitchRoll(): YawPitchRollAngles {
	val orientation = toOrientation().toAxesOrder(AxesOrder.ZXY).toAxesReference(AxesReference.EXTRINSIC)
	return YawPitchRollAngles(orientation.angleUnit, orientation.firstAngle.toDouble(), orientation.secondAngle.toDouble(), orientation.thirdAngle.toDouble(), orientation.acquisitionTime)
}
