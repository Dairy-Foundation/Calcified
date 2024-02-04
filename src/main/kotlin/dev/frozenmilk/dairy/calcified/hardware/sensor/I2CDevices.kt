package dev.frozenmilk.dairy.calcified.hardware.sensor

import com.qualcomm.robotcore.hardware.LynxModuleImuType
import com.qualcomm.robotcore.hardware.configuration.LynxConstants
import dev.frozenmilk.dairy.calcified.CalcifiedDeviceMap
import dev.frozenmilk.dairy.calcified.hardware.CalcifiedModule
import dev.frozenmilk.util.units.orientation.AngleBasedRobotOrientation

class I2CDevices internal constructor(module: CalcifiedModule) : CalcifiedDeviceMap<Any>(module){
	fun getIMU(port: Byte, imuType: LynxModuleImuType, angleBasedRobotOrientation: AngleBasedRobotOrientation): CalcifiedIMU {
		if (port !in 0 until LynxConstants.NUMBER_OF_I2C_BUSSES) throw IllegalArgumentException("$port is not in the acceptable port range [0, ${LynxConstants.NUMBER_OF_I2C_BUSSES - 1}]")
		if (!this.containsKey(port) || this[port] !is CalcifiedIMU || (this[port] as CalcifiedIMU).imuType != imuType) {
			this[port] = CalcifiedIMU(imuType, module, port, angleBasedRobotOrientation)
		}
		return (this[port] as CalcifiedIMU)
	}

	@JvmOverloads
	fun getIMU_BHI260(port: Byte, angleBasedRobotOrientation: AngleBasedRobotOrientation = AngleBasedRobotOrientation()) = this.getIMU(port, LynxModuleImuType.BHI260, angleBasedRobotOrientation)

	@JvmOverloads
	fun getIMU_BNO055(port: Byte, angleBasedRobotOrientation: AngleBasedRobotOrientation = AngleBasedRobotOrientation()) = this.getIMU(port, LynxModuleImuType.BNO055, angleBasedRobotOrientation)
}