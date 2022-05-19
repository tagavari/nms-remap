package me.tagavari.nmsremap

import net.md_5.specialsource.Jar
import net.md_5.specialsource.JarMapping
import net.md_5.specialsource.JarRemapper
import net.md_5.specialsource.provider.JarProvider
import net.md_5.specialsource.provider.JointProvider
import org.gradle.api.Project
import java.io.File

internal fun remapJar(
	project: Project,
	inputFile: File,
	outputFile: File,
	srgIn: String,
	remappedDependencies: List<String>,
	reverse: Boolean = false,
	excludedPackages: List<String> = listOf(),
	numeric: Boolean = false,
	generateAPI: Boolean = false,
	logFile: File? = null
) {
	//Load dependencies
	val mappingFile = project.configurations.detachedConfiguration(
		project.dependencies.create(srgIn)
	).singleFile
	val inheritanceFileList = remappedDependencies.map { dependency ->
		project.configurations.detachedConfiguration(
			project.dependencies.create(dependency)
		).files.toList()
	}.flatten()
	
	//Create the jar mapping
	val mapping = JarMapping()
	
	//Register excluded packages
	for(packageName in excludedPackages) {
		mapping.addExcludedPackage(packageName)
	}
	
	//Load mappings
	mapping.loadMappings(
		mappingFile.canonicalPath,
		reverse,
		numeric,
		null,
		null
	)
	
	val jointProvider = JointProvider()
	val loadedJars = mutableListOf<Jar>()
	try {
		//Load remapped dependencies for inheritance lookup
		for(inheritanceFile in inheritanceFileList) {
			project.logger.info("Adding inheritance ${inheritanceFile.path}")
			val jar = Jar.init(inheritanceFile)
			jointProvider.add(JarProvider(jar))
			loadedJars.add(jar)
		}
		
		//Load the main jar file
		val archiveFileJar = Jar.init(inputFile).also { jar ->
			jointProvider.add(JarProvider(jar))
			loadedJars.add(jar)
		}
		
		mapping.setFallbackInheritanceProvider(jointProvider)
		
		//Do the remap
		val remapper = JarRemapper(null, mapping, null)
		remapper.setGenerateAPI(generateAPI)
		logFile?.let { remapper.setLogFile(it) }
		remapper.remapJar(archiveFileJar, outputFile)
	} finally {
		//Make sure we close all loaded jars
		loadedJars.forEach { it.close() }
	}
}