package dev.frozenmilk.dairy.calcified.hardware.sensor

import com.qualcomm.hardware.lynx.commands.core.LynxSetDIODirectionCommand
import com.qualcomm.robotcore.hardware.DigitalChannel
import dev.frozenmilk.dairy.calcified.hardware.CalcifiedModule
import dev.frozenmilk.dairy.core.util.supplier.logical.EnhancedBooleanSupplier
import dev.frozenmilk.dairy.core.util.supplier.logical.IEnhancedBooleanSupplier

class CalcifiedDigitalInput(val module: CalcifiedModule, val port: Int) : IEnhancedBooleanSupplier by EnhancedBooleanSupplier({ module.bulkData.getDigitalInput(port) }) {
	init {
		LynxSetDIODirectionCommand(module.lynxModule, port, DigitalChannel.Mode.INPUT).send()
	}
}