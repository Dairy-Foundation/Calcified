package dev.frozenmilk.dairy.calcified.hardware.sensor

import com.qualcomm.hardware.lynx.commands.core.LynxSetDIODirectionCommand
import com.qualcomm.hardware.lynx.commands.core.LynxSetSingleDIOOutputCommand
import com.qualcomm.robotcore.hardware.DigitalChannel
import dev.frozenmilk.dairy.calcified.hardware.CalcifiedModule
import java.util.function.Consumer

class CalcifiedDigitalOutput(private val module: CalcifiedModule, private val port: Byte) : Consumer<Boolean> {
	init {
		LynxSetDIODirectionCommand(module.lynxModule, port.toInt(), DigitalChannel.Mode.OUTPUT).send()
	}
	override fun accept(p0: Boolean) {
		LynxSetSingleDIOOutputCommand(module.lynxModule, port.toInt(), p0).send()
	}
}
