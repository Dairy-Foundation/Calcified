package dev.frozenmilk.dairy.calcified.hardware.sensor

import dev.frozenmilk.dairy.calcified.hardware.CalcifiedModule
import dev.frozenmilk.dairy.core.util.supplier.numeric.EnhancedDoubleSupplier

class CalcifiedAnalogInput(val module: CalcifiedModule, val port: Byte) : EnhancedDoubleSupplier({ module.bulkData.getAnalogInput(port.toInt()).toDouble() })