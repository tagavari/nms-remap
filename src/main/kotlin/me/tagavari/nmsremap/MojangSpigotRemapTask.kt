package me.tagavari.nmsremap

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import java.nio.file.Files

/**
 * An easy-to-use task that simply remaps a jar
 * file from Mojang to Spigot mappings
 */
abstract class MojangSpigotRemapTask : DefaultTask() {
	/**
	 * The Minecraft version number to use (ex. '1.18.2-R0.1-SNAPSHOT')
	 * If this property is left unset, it will be automatically resolved
	 * to the version number of an 'org.spigotmc:spigot' compileOnly dependency
	 */
	@get:Input
	@get:Optional
	val version: Property<String> = project.objects.property(String::class.java)
		.convention(project.providers.provider {
			//Find the spigot module
			val spigotDependency = project.configurations.getByName("compileOnly")
				.allDependencies
				.firstOrNull { dependency ->
					dependency.group == "org.spigotmc" && dependency.name == "spigot"
				} ?: throw Exception("Dependency on org.spigotmc:spigot not found; cannot autodetect version")
			
			//Get the version
			spigotDependency.version
				?: throw throw Exception("Failed to determine version number from org.spigotmc:spigot; cannot autodetect version")
		})
	
	/**
	 * The jar file to take as input and remap
	 */
	@get:InputFile
	@get:Optional
	val inputFile: RegularFileProperty = project.objects.fileProperty().convention(
		(project.tasks.named("jar").get() as AbstractArchiveTask).archiveFile
	)
	
	/**
	 * The classifier to add to the end of the output file
	 * (if archiveName is not specified)
	 */
	@get:Input
	@get:Optional
	abstract val archiveClassifier: Property<String>
	
	/**
	 * The name to use for the output file
	 * (if outputFile is not specified)
	 */
	@get:Input
	@get:Optional
	abstract val archiveName: Property<String>
	
	/**
	 * The archive file to write to
	 */
	@get:OutputFile
	@get:Optional
	val outputFile: RegularFileProperty = project.objects.fileProperty().convention {
		resolveArchiveFile(
			inputFile.asFile.get(),
			archiveName.orNull,
			archiveClassifier.orNull
		)
	}
	
	@TaskAction
	fun execute() {
		val archiveFile = inputFile.asFile.get()
		val targetFile = outputFile.asFile.get()
		val versionStr = version.get()
		
		logger.info("Remapping Mojang -> Spigot using version $versionStr")
		
		//Pick a random file for writing to temporarily
		val obfFile = Files.createTempFile(null, ".jar").toFile()
		
		try {
			//Map Mojang -> obfuscated
			remapJar(
				project = project,
				inputFile = archiveFile,
				outputFile = obfFile,
				srgIn = "org.spigotmc:minecraft-server:$versionStr:maps-mojang@txt",
				remappedDependencies = listOf("org.spigotmc:spigot:$versionStr:remapped-mojang"),
				reverse = true
			)
			
			//Delete the target file to replace it
			Files.deleteIfExists(targetFile.toPath())
			
			//Map obfuscated -> Spigot
			remapJar(
				project = project,
				inputFile = obfFile,
				outputFile = targetFile,
				srgIn = "org.spigotmc:minecraft-server:$versionStr:maps-spigot@csrg",
				remappedDependencies = listOf("org.spigotmc:spigot:$versionStr:remapped-obf"),
				reverse = false
			)
		} finally {
			//Clean up
			obfFile.delete()
		}
	}
}