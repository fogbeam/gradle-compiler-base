package com.smokejumperit.gradle.compilers;

import org.gradle.api.tasks.compile.*;
import org.gradle.api.*;
import org.gradle.api.file.*;
import org.gradle.api.internal.file.*;
import org.gradle.api.internal.tasks.*;
import org.gradle.api.plugins.*;
import org.gradle.api.specs.*;
import org.gradle.api.tasks.*;
import org.gradle.api.tasks.util.*;
import org.gradle.util.*;
import org.gradle.api.artifacts.Configuration;
import java.util.*;
import java.io.*;

class GeneratorPluginHelper {

	static void copyGeneratedFiles(Project project, Collection<String> fileSuffixes, FileTree source, File destDir) {
		project.ant.copy(
			toDir:destDir.absolutePath, 
			verbose:true,
			failOnError:true,
			preserveLastModified:true
		) {
			dirs(source).each { File dirFile ->
				project.logger.info("Copying files with suffixes " + fileSuffixes +" from " + dirFile + " to " + destDir)
				fileset(dir:dirFile.absolutePath) {
					fileSuffixes.each { String suffix ->
						include(name:"**/*.${suffix}")
					}
				}
			}
		}
	}

	static SortedSet<File> dirs(FileTree source) {
		source.collect { File file ->
			if(file.isDirectory()) {
				return file
			} else {
				return file.getParent()
			}
		}*.canonicalFile as SortedSet
	}

}
