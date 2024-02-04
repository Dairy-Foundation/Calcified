package dev.frozenmilk.dairy.calcified.hardware.pwm

import com.qualcomm.robotcore.hardware.PwmControl

interface PWMDevice {
	var pwmRange: PwmControl.PwmRange
}