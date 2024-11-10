plugins {
	kotlin("jvm") version "2.0.21"
	id("java-gradle-plugin")
	id("com.gradle.plugin-publish") version "1.3.0"
	id("maven-publish")
}

version = "1.0.1"
group = "me.tagavari"

repositories {
	mavenCentral()
}

dependencies {
	//SpecialSource
	implementation("net.md-5:SpecialSource:1.11.4")
}

gradlePlugin {
	website = "https://github.com/tagavari/nms-remap"
	vcsUrl = "https://github.com/tagavari/nms-remap"
	
	plugins {
		create("nmsRemap") {
			id = "me.tagavari.nmsremap"
			implementationClass = "me.tagavari.nmsremap.NMSRemapPlugin"
			displayName = "NMS remap plugin"
			description = "Use SpecialSource to remap Spigot plugin outputs"
			tags = listOf("minecraft", "specialsource", "nms", "spigot", "mojang")
		}
	}
}
