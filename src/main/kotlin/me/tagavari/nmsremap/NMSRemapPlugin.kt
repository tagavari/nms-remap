package me.tagavari.nmsremap

import org.gradle.api.Plugin
import org.gradle.api.Project

class NMSRemapPlugin : Plugin<Project> {
	override fun apply(project: Project) {
		project.tasks.register("remap", MojangSpigotRemapTask::class.java)
	}
}