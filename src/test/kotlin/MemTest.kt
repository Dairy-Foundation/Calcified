//import dev.frozenmilk.dairy.calcified.hardware.controller.ComplexController
//import dev.frozenmilk.dairy.calcified.hardware.controller.ErrorSupplier
//import dev.frozenmilk.dairy.calcified.hardware.controller.LambdaController
//import dev.frozenmilk.dairy.calcified.hardware.motor.CalcifiedEncoder
//import dev.frozenmilk.dairy.calcified.hardware.motor.Direction
//import dev.frozenmilk.dairy.calcified.hardware.motor.SimpleMotor
//import dev.frozenmilk.dairy.core.Feature
//import dev.frozenmilk.dairy.core.FeatureRegistrar
//import org.junit.Test
//
//class ControllerMemSafety {
//	lateinit var controller: ComplexController<Int>
//
//	fun makeController(): ComplexController<Int> {
//
//		val motor = object : SimpleMotor {
//			override var direction: Direction = Direction.FORWARD
//			override var cachingTolerance: Double = 0.005
//			override var enabled: Boolean = true
//			override var power: Double = 0.0
//		}
//
//		val errorSupplier = object : ErrorSupplier<Int, Double> {
//			override fun findError(target: Int): Double = 0.0
//		}
//
//
//		val controller = LambdaController(0)
//				// attaches a mix of motors and cr servos to be updated by the controller
//				.addMotors(motor)
//				// says to use the position supplier on the encoder for each of the following
//				.withErrorSupplier(errorSupplier)
//				// adds a P term
//				.appendPController(0.1)
//				// adds an I term
//				.appendIController(0.00001, 0.0, 0.2)
//				// adds a D term
//				.appendDController(0.005)
//
//		controller.target = 100
//
//		return controller
//	}
//
//	@Test
//	fun memSafety() {
//		FeatureRegistrar.cleanFeatures()
//
//		controller = makeController()
//
//		FeatureRegistrar.registrationQueue.addAll(FeatureRegistrar.registeredFeatures)
//		FeatureRegistrar.resolveRegistrationQueue()
//
//		println("stop")
//	}
//}