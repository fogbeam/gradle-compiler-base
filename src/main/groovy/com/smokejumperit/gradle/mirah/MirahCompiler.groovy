package com.smokejumperit.gradle;

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileTreeElement
import org.gradle.api.internal.tasks.DefaultScalaSourceSet
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.compile.*
import org.gradle.api.tasks.*
import com.smokejumperit.gradle.mirah.*
import com.smokejumperit.gradle.compiler.*
import org.mirah.MirahCommand

public class MirahCompile extends AbstractCompile {

	protected void compile() {
		def args = []

		if(project.logger.isDebugEnabled() || project.logger.isTraceEnabled()) {
			args << "-V" // Verbose logging
		}

		// Compile within the build dir
		args << "--cd"
		args << project.buildDir.absolutePath

		// Add destination to args
		args << "--dest"
		args << destinationDir.absolutePath

		// Add classpath to args
		args << "--classpath"
		args << project.configurations['compile'].asPath

		// Add the files to be compiled to the args
		args.addAll(source.files.collect { it.absolutePath })

		MirahCommand.main(args as String[])
	}

}
