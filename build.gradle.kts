plugins {
	kotlin("jvm") version "1.6.21"
	id("java-gradle-plugin")
	id("com.gradle.plugin-publish") version "1.0.0-rc-2"
	id("maven-publish")
}

version = "1.0.0"
group = "me.tagavari"

repositories {
	mavenCentral()
}

dependencies {
	//SpecialSource
	implementation("net.md-5:SpecialSource:1.11.4")
}

gradlePlugin {
	plugins {
		create("nmsRemap") {
			id = "me.tagavari.nmsremap"
			implementationClass = "me.tagavari.nmsremap.NMSRemapPlugin"
			displayName = "NMS remap plugin"
		}
	}
}

pluginBundle {
	website = "https://github.com/tagavari/nms-remap"
	vcsUrl = "https://github.com/tagavari/nms-remap"
	
	description = "Use SpecialSource to remap Spigot plugin outputs"
	tags = listOf("minecraft", "specialsource", "nms", "spigot", "mojang")
}