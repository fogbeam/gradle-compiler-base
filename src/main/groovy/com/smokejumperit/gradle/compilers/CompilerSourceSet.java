package com.smokejumperit.gradle.compilers;

import org.gradle.api.file.SourceDirectorySet;

public abstract class CompilerSourceSet {
	protected final String name;
	protected volatile SourceDirectorySet compilerSourceDirSet, allSourceDirSet;

	protected CompilerSourceSet(final String name) {
		this.name = name;
	}

	public String getName() { return name; } 

	public SourceDirectorySet getCompilerSourceDirSet() {
		return compilerSourceDirSet;
	}

	public SourceDirectorySet getAllSourceDirSet() {
		return allSourceDirSet;
	}

}


