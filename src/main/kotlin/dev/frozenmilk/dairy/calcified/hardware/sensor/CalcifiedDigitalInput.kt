package dev.frozenmilk.dairy.calcified.hardware.sensor

import com.qualcomm.hardware.lynx.commands.core.LynxSetDIODirectionCommand
import com.qualcomm.robotcore.hardware.DigitalChannel
import dev.frozenmilk.dairy.calcified.hardware.CalcifiedModule
import dev.frozenmilk.dairy.core.util.supplier.logical.EnhancedBooleanSupplier
import dev.frozenmilk.dairy.core.util.supplier.logical.IEnhancedBooleanSupplier

class CalcifiedDigitalInput(val module: CalcifiedModule, val port: Byte) : IEnhancedBooleanSupplier by EnhancedBooleanSupplier({ module.bulkData.getDigitalInput(port.toInt()) }) {
	init {
		LynxSetDIODirectionCommand(module.lynxModule, port.toInt(), DigitalChannel.Mode.INPUT).send()
	}
}