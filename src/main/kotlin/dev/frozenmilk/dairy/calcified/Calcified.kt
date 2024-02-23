package dev.frozenmilk.dairy.calcified

import com.qualcomm.hardware.lynx.LynxModule
import com.qualcomm.robotcore.hardware.configuration.LynxConstants
import dev.frozenmilk.dairy.calcified.hardware.CalcifiedModule
import dev.frozenmilk.dairy.core.DairyCore
import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.FeatureRegistrar
import dev.frozenmilk.dairy.core.dependencyresolution.dependencyset.DependencySet
import dev.frozenmilk.dairy.core.wrapper.Wrapper
import dev.frozenmilk.util.cell.LazyCell
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta.Flavor
import java.lang.annotation.Inherited

/**
 * enabled by having either @[DairyCore] or @[Attach]
 */
object Calcified : Feature {
	/**
	 * @see Attach.automatedCacheHandling
	 */
	@JvmStatic
	var automatedCacheHandling = true
		private set

	/**
	 * enabled by having either @[DairyCore] or @[Attach]
	 */
	override val dependencies = DependencySet(this)
			.includesExactlyOneOf(DairyCore::class.java, Attach::class.java).bindOutputTo {
				automatedCacheHandling = when (it) {
					is Attach -> {
						it.automatedCacheHandling
					}

					else -> {
						true
					}
				}
			}

	/**
	 * all calcified modules found this OpMode
	 */
	@JvmStatic
	var modules: Array<CalcifiedModule> = emptyArray()
		private set

	private val controlHubCell = LazyCell {
		if (!FeatureRegistrar.opModeActive) throw IllegalStateException("OpMode not inited, cannot yet access the control hub")
		modules.filter { it.lynxModule.isParent && LynxConstants.isEmbeddedSerialNumber(it.lynxModule.serialNumber) }.getOrNull(0) ?:throw IllegalStateException(("The control hub was not found, this may be an electronics issue"))
	}

	/**
	 * the first hub in [modules] that satisfies the conditions to be considered a control hub
	 */
	@JvmStatic
	val controlHub: CalcifiedModule by controlHubCell

	private val expansionHubCell = LazyCell {
		if (!FeatureRegistrar.opModeActive) throw IllegalStateException("OpMode not inited, cannot yet access the expansion hub")
		modules.filter { !(it.lynxModule.isParent && LynxConstants.isEmbeddedSerialNumber(it.lynxModule.serialNumber)) }.getOrNull(0) ?: throw IllegalStateException(("The expansion hub was not found, this may be an electronics issue"))
	}

	/**
	 * the first hub in [modules] that satisfies the conditions to be considered an expansion hub
	 */
	@JvmStatic
	val expansionHub: CalcifiedModule by expansionHubCell

	/**
	 * internal refresh caches, only refreshes if the automated process is enabled
	 */
	private fun refreshCaches() {
		if (automatedCacheHandling) modules.forEach { it.refreshBulkCache() }
	}

	/**
	 * should be run in stop if you want to clear the status of the hardware objects for the next user, otherwise modules and hardware will be cleared according to [crossPollinate]
	 */
	@JvmStatic
	fun clearModules() {
		modules = emptyArray()
	}

	override fun preUserInitHook(opMode: Wrapper) {
		modules = opMode.opMode.hardwareMap.getAll(LynxModule::class.java).map {
			CalcifiedModule(it)
		}.toTypedArray()

		controlHubCell.invalidate()
		expansionHubCell.invalidate()

		refreshCaches()
	}

	override fun postUserInitHook(opMode: Wrapper) {
	}

	override fun preUserInitLoopHook(opMode: Wrapper) {
		refreshCaches()
	}

	override fun postUserInitLoopHook(opMode: Wrapper) {
	}

	override fun preUserStartHook(opMode: Wrapper) {
		refreshCaches()
	}


	override fun postUserStartHook(opMode: Wrapper) {
	}

	override fun preUserLoopHook(opMode: Wrapper) {
		refreshCaches()
	}

	override fun postUserLoopHook(opMode: Wrapper) {
	}

	override fun preUserStopHook(opMode: Wrapper) {
		refreshCaches()
	}

	@Retention(AnnotationRetention.RUNTIME)
	@Target(AnnotationTarget.CLASS)
	@MustBeDocumented
	@Inherited
	annotation class Attach(
			/**
			 * Controls if the caches are automatically handled by [Calcified] or not
			 *
			 * Set to false if you want to handle the clearing of the module caches by hand
			 *
			 * Clearing the caches should probably be done using [CalcifiedModule.refreshBulkCache]
			 */
			val automatedCacheHandling: Boolean = true,
	)
}
