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

public abstract class GeneratorPlugin<GENERATOR_TYPE extends AbstractGenerator> 
	extends CompilerPlugin<GENERATOR_TYPE> implements Plugin<Project> 
{

	@Override
	protected boolean dependsOnCompileJava() { return false; }

	@Override
	public void postConfig(GENERATOR_TYPE generatorTask, SourceSet sourceSet, Project project) {
		File genDir = new File(project.getBuildDir(), getName() + "/" + sourceSet.getName() + "/" + getCompilesToLanguage() + "-gen");
		generatorTask.setDestinationDir(genDir);
		AbstractCompile compileTask = 
			project.getTasks().withType(AbstractCompile.class).getByName(sourceSet.getCompileTaskName(getCompilesToLanguage()));
		compileTask.dependsOn(generatorTask);
		compileTask.source(genDir);
		super.postConfig(generatorTask, sourceSet, project);
	}

	public abstract String getCompilesToLanguage();

}
