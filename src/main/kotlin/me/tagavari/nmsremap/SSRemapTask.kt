package me.tagavari.nmsremap

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/**
 * Provides an API similar to specialsource-maven-plugin
 * for remapping jar files
 * https://github.com/agaricusb/SpecialSourceMP
 */
abstract class SSRemapTask : DefaultTask() {
	@get:Input
	abstract val srgIn: Property<String>
	
	@get:Input
	@get:Optional
	abstract val reverse: Property<Boolean>
	
	@get:Input
	abstract val remappedDependencies: ListProperty<String>
	
	@get:InputFile
	@get:Optional
	val inputFile: RegularFileProperty = project.objects.fileProperty().convention(
		(project.tasks.named("jar").get() as AbstractArchiveTask).archiveFile
	)
	
	@get:Input
	@get:Optional
	abstract val archiveName: Property<String>
	
	@get:Input
	@get:Optional
	abstract val archiveClassifier: Property<String>
	
	@get:OutputFile
	@get:Optional
	val outputFile: RegularFileProperty = project.objects.fileProperty().convention {
		resolveArchiveFile(
			inputFile.asFile.get(),
			archiveName.orNull,
			archiveClassifier.orNull
		)
	}
	
	@get:Input
	@get:Optional
	abstract val excludedPackages: ListProperty<String>
	
	@get:Input
	@get:Optional
	abstract val numeric: Property<Boolean>
	
	@get:Input
	@get:Optional
	abstract val generateAPI: Property<Boolean>
	
	@get:OutputFile
	@get:Optional
	abstract val logFile: RegularFileProperty
	
	@TaskAction
	fun execute() {
		val archiveFile = inputFile.asFile.get()
		val targetFile = outputFile.asFile.get()
		
		//Pick a random file for writing to temporarily
		val writeFile = Files.createTempFile(null, ".jar").toFile()
		
		remapJar(
			project = project,
			inputFile = archiveFile,
			outputFile = writeFile,
			srgIn = srgIn.get(),
			remappedDependencies = remappedDependencies.get(),
			reverse = reverse.orNull ?: false,
			excludedPackages = excludedPackages.orNull ?: listOf(),
			numeric = numeric.orNull ?: false,
			generateAPI = generateAPI.orNull ?: false,
			logFile = logFile.asFile.orNull
		)
		
		//Move the completed file to the target file
		Files.move(writeFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
	}
}