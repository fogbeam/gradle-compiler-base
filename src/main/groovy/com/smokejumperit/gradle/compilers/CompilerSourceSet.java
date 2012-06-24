package com.smokejumperit.gradle.compilers;

import java.util.concurrent.Callable;

import org.gradle.api.file.*;
import org.gradle.api.internal.file.*;
import org.gradle.util.*;

import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.file.SourceDirectorySet;

public class CompilerSourceSet {
	protected final String name;
	protected final SourceDirectorySet compilerSourceDirSet, allSourceDirSet;

	protected CompilerSourceSet(final CompilerPlugin<?> plugin, final Project project, final SourceSet ss) {
    name = plugin.getName();
    final String capName = name.substring(0,1).toUpperCase() + name.substring(1);
    final String lowName = name.substring(0,1).toLowerCase() + name.substring(1);

		final String ssName = ss.getName();

		compilerSourceDirSet = new DefaultSourceDirectorySet(
			ssName + " " + lowName + "/supporting",
			ssName + " " + capName + " compiler consumed source",
			(FileResolver)project.property("fileResolver")
		);
		compilerSourceDirSet.srcDir("src/" + ssName + "/" + name);

		allSourceDirSet = new DefaultSourceDirectorySet(
			ssName + " " + lowName,
			ssName + " " + capName + " source",
			(FileResolver)project.property("fileResolver")
		);
		allSourceDirSet.source(compilerSourceDirSet);

		for(String suffix : plugin.getAdditionalConsumedFileSuffixes()) {
			final String havingSuffix = "**/*" + suffix;
			compilerSourceDirSet.getFilter().include(havingSuffix);
		}

		for(String suffix : plugin.getLanguageFileSuffixes()) {
			final String havingSuffix = "**/*" + suffix;
			compilerSourceDirSet.getFilter().include(havingSuffix);
			allSourceDirSet.getFilter().include(havingSuffix);
		}
	}

	public String getName() { return name; } 

	public SourceDirectorySet getCompilerSourceDirSet() {
		return compilerSourceDirSet;
	}

	public SourceDirectorySet getAllSourceDirSet() {
		return allSourceDirSet;
	}

}


