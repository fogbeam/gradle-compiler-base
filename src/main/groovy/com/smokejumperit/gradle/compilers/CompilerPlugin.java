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
import static com.smokejumperit.gradle.compilers.CompilerPluginHelper.*;

public abstract class CompilerPlugin<COMPILE_TYPE extends AbstractCompile> implements Plugin<Project> {

	/**
	* Convenience method providing access to the JavaPluginConvention, which is where source sets reside.
	*/
	protected static final JavaPluginConvention getJavaPluginConvention(Project project) {
		return (JavaPluginConvention)(project.getConvention().getPlugins().get("java"));
	}

	/**
	* The file suffixes specific to the language of this compiler. 
	*/
	protected abstract Collection<String> getLanguageFileSuffixes();

	/**
	* Additional file suffixes consumed by the compiler but not unique to it. For example, both Groovy and Scala also consume 
	* <code>*.java</code> files. May return either <code>null</code> or an empty list to denote that only language file suffixes
	* are consumed.
	*/
	protected abstract Collection<String> getAdditionalConsumedFileSuffixes();

	/**
	* The short name, required for generating folders, properties, etc.
	*/
	protected abstract String getName();

	/**
	* Provides the compile task class for this compiler.
	*/
	protected abstract Class<COMPILE_TYPE> getCompileTaskClass();

	/**
	* Extra configurations to be added to the compiler classpath. Default implementation is to not apply anything. 
	*/
	protected Collection<String> getCompileConfigurationNames() { return Collections.emptyList(); }

	/**
	* Method to override if you want to do some post-processing configuration on each SourceSet.
	*/
	protected void postConfig(COMPILE_TYPE  task, SourceSet set, Project project) {}

	/**
	* Implements the application of the plugin to the project.
	*/
	public void apply(Project project) {
		project.getPlugins().apply(JavaPlugin.class);
		JavaBasePlugin javaPlugin = project.getPlugins().apply(JavaBasePlugin.class);

		configureSourceSetsDefaults(project, javaPlugin);
	}

	protected static String getDisplayName(SourceSet ss) {
		if(ss instanceof DefaultSourceSet) {
			return ((DefaultSourceSet)ss).getDisplayName();
		} else {
			return ss.getName();
		}
	}

	protected void configureSourceSetsDefaults(final Project project, final JavaBasePlugin javaPlugin) {
		final CompilerPlugin me = this;
		final String name = getName();
		getJavaPluginConvention(project).getSourceSets().all(new Action<SourceSet>() {
			public void execute(SourceSet ss) {
				final CompilerSourceSet css = applyConvention(ss, name, createCompilerSourceSet(me, project, getDisplayName(ss)));
				css.getCompilerSourceDirSet().srcDir(project.file("src/" + ss.getName() + "/" + name));
				ss.getAllJava().source(css.getCompilerSourceDirSet());
				ss.getAllSource().source(css.getCompilerSourceDirSet());
				ss.getResources().getFilter().exclude(new Spec<FileTreeElement>() {
					public boolean isSatisfiedBy(FileTreeElement elt) {
						return css.getCompilerSourceDirSet().contains(elt.getFile());
					}
				});

				final String taskName = ss.getCompileTaskName(name);
				COMPILE_TYPE compile = project.getTasks().add(taskName, getCompileTaskClass());
				compile.dependsOn(ss.getCompileJavaTaskName());
				javaPlugin.configureForSourceSet(ss, compile);
				compile.setDescription("Compiles the " + css.getCompilerSourceDirSet() + ".");
				compile.source(css.getCompilerSourceDirSet());

				project.getTasks().getAt(ss.getClassesTaskName()).dependsOn(taskName);

				postConfig(compile, ss, project);
			}
		});
	}

}
