package com.smokejumperit.gradle;

import org.gradle.api.tasks.*
import org.gradle.api.tasks.compile.*
import com.smokejumperit.gradle.mirah.*
import com.smokejumperit.gradle.compiler.*
import org.mirah.MirahCommand

public class MirahCompile extends AbstractCompile {

	protected void compile() {
		def args = ["compile"]

		if(project.logger.isDebugEnabled() || project.logger.isTraceEnabled()) {
			args << "-V" // Verbose logging
		}

/*
		// Need to move to where the paths are defined
		args << "--cd"
		args << 
*/

		// Add destination to args
		args << "--dest"
		args << destinationDir.absolutePath

		// Add classpath to args
		args << "--classpath"
		args << project.configurations['compile'].asPath

		// Add the files to be compiled to the args
		args.addAll(source.files*.absolutePath)

		MirahCommand.main(args as String[])
	}

}
