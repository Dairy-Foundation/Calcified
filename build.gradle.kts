plugins {
	id("com.android.library")
	id("kotlin-android")
	id("org.jetbrains.dokka") version "1.9.10"
	id("maven-publish")
}

android {
	namespace = "dev.frozenmilk.dairy.calcified"
	compileSdk = 29

	defaultConfig {
		minSdk = 24
		//noinspection ExpiredTargetSdkVersion
		targetSdk = 28

		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
		consumerProguardFiles("consumer-rules.pro")
	}

	compileSdk = 29

	buildTypes {
		release {
			isMinifyEnabled = false
			proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
		}
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_1_8
		targetCompatibility = JavaVersion.VERSION_1_8

		kotlin {
			compilerOptions {
				freeCompilerArgs.add("-Xjvm-default=all")
			}
		}
	}
}

repositories {
	maven("https://www.jitpack.io")
}

dependencies {
	//noinspection GradleDependency
	implementation("androidx.appcompat:appcompat:1.2.0")
	testImplementation("org.testng:testng:6.9.6")

	compileOnly(project(":Core"))
	testImplementation(project(":Core"))

	compileOnly("com.github.Dairy-Foundation:CachingHardware:v1")
	compileOnly("org.firstinspires.ftc:RobotCore:9.1.0")
	compileOnly("org.firstinspires.ftc:Hardware:9.1.0")
	compileOnly("org.firstinspires.ftc:FtcCommon:9.1.0")
}

publishing {
	publications {
		register<MavenPublication>("release") {
			groupId = "dev.frozenmilk.dairy"
			artifactId = "Calcified"
			version = "v0.0.0"

			afterEvaluate {
				from(components["release"])
			}
		}
	}
	repositories {
		maven {
			name = "Calcified"
			url = uri("${project.buildDir}/release")
		}
	}
}
