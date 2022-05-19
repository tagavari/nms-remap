package me.tagavari.nmsremap

import java.io.File

/**
 * Resolves the target file in the order of priority:
 * 1 - The "archiveName" property
 * 2 - The "archiveClassifier" property
 * 3 - The target task output file (overwrite)
 */
internal fun resolveArchiveFile(
	inputFile: File,
	archiveName: String?,
	archiveClassifier: String?
): File {
	return archiveName?.let { name ->
		File(inputFile.parent, name)
	} ?: archiveClassifier?.let { classifier ->
		File(
			inputFile.parent,
			"${inputFile.nameWithoutExtension}-$classifier.${inputFile.extension}"
		)
	} ?: inputFile
}