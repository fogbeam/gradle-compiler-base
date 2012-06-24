package com.smokejumperit.gradle.compilers;

import org.gradle.api.tasks.compile.*;
import org.gradle.api.*;
import org.gradle.api.file.*;
import org.gradle.api.internal.file.*;
import org.gradle.api.plugins.*;
import org.gradle.api.specs.*;
import org.gradle.api.tasks.*;
import org.gradle.api.tasks.util.*;
import org.gradle.util.*;
import org.gradle.api.artifacts.Configuration;

class CompilerPluginHelper {

	static CompilerSourceSet applyConvention(SourceSet ss, String name, CompilerSourceSet css) {
		ss.convention.plugins."$name" = css
		return css
	}

	static CompilerSourceSet createCompilerSourceSet(CompilerPlugin compilerPlugin, Project project, String sourceSetName) {
		final String name = compilerPlugin.name
		final String capName = "${name[0].toUpperCase()}${name[1..-1]}"
		final String lowName = "${name[0].toLowerCase()}${name[1..-1]}"
		return Eval.xy(compilerPlugin, project, """
			import org.gradle.api.file.*
			import org.gradle.api.internal.file.*
			import org.gradle.util.*
			import com.smokejumperit.gradle.compilers.CompilerSourceSet

			class ${capName}SourceSet extends CompilerSourceSet {

				${capName}SourceSet() {
					super("${lowName}")

					def fileResolver = y.fileResolver
		
					def filesWithSuffix = { suffix -> '**/*.' + suffix }
		
					compilerSourceDirSet = new DefaultSourceDirectorySet("${sourceSetName} ${capName}-compiler consumed source", fileResolver)
					compilerSourceDirSet.filter.include(x.languageFileSuffixes.collect(filesWithSuffix))
					compilerSourceDirSet.filter.include(x.additionalConsumedFileSuffixes.collect(filesWithSuffix))
					
					allSourceDirSet = new DefaultSourceDirectorySet("${sourceSetName} ${capName} source", fileResolver)
					allSourceDirSet.filter.include(x.languageFileSuffixes.collect(filesWithSuffix))
				}

				SourceDirectorySet get${capName}() { compilerSourceDirSet }

				${capName}SourceSet ${lowName}(Closure configureClosure) {
					ConfigureUtil.configure(configureClosure, compilerSourceDirSet);
					return this;
				}

				SourceDirectorySet getAll${capName}() { allSourceDirSet }
			}

			return new ${capName}SourceSet()
		"""
		);
	}

}
