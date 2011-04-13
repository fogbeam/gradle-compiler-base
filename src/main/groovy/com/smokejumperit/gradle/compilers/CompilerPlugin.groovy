package com.smokejumperit.gradle;

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileTreeElement
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import com.smokejumperit.gradle.mirah.*

public abstract class CompilerPlugin implements Plugin<Project> {

	/**
	* Provides an instance of the source set convention.
	*/
	protected abstract CompilerSourceSetConvention getSourceSetConvention(String displayName, FileResolver resolver);

	/**
	* The short name, required for generating folders, properties, etc.
	*/
	protected abstract String getName();

	/**
	* Provides the compile class task name
	*/
	protected abstract Class<AbstractCompile> getCompileTaskClass();

	/**
	* Provides the documentation class task name. May be <code>null</code> if there is no documentation class.
	*/
	protected abstract Class<AbstractDoc> getDocTaskClass();

	/**
	* Extra configurations to be added to the compiler. Return <code>null</code> or an empty list if none.
	*/
	protected abstract Collection<String> getCompileConfigurationNames();

	/**
	* Whether or not there is a documentation class that goes with this compiler. Checks if {@link #getDocTaskClass()} is 
	* not <code>null</code>.
	*/
	public final boolean hasDoc() {
		return getDocTaskClass() != null
	}

	public void apply(Project project) {
		project.plugins.apply(JavaPlugin.class)
		JavaBasePlugin javaPlugin = project.plugins.apply(JavaBasePlugin.class)

		configureSourceSetDefaults(project, javaPlugin)
		if(hasDoc()) addDocTask(project)
		configureCompileDefaults(project)
		configureCompilerConfigurations(project)
	}

	protected void configureDoc(AbstractDoc doc, Project project) {}

	protected void addDocTask(Project project) {
		project.tasks.withType(getDocTaskClass()) { AbstractDoc doc ->
			doc.conventionMapping.classpath = { project.sourceSets.main.classes + project.sourceSets.main.compileClasspath }
			doc.conventionMapping.defaultSource = { project.sourceSets.main."${getName()}" }
			configureDoc(doc, project)
		}
		AbstractDoc doc = project.tasks.add("${getName()}doc", getDocTaskClass())
		doc.description = "Generates the ${getName()} doc for the main source code"
		doc.group = JavaBasePlugin.DOCUMENTATION_GROUP
	}

	protected void configureSourceSetsDefaults(Project project, JavaBasePlugin javaPlugin) {
		String name = getName();
		project.convention.getPlugin(JavaPluginConvention.class).sourceSets.all { SourceSet sourceSet ->
			configureSourceSetDefaults(sourceSet, project, javaPlugin)
		}
	}

	protected void configureSourceSetDefaults(SourceSet sourceSet, Project project, JavaBasePlugin javaPlugin) {
		String name = getName()
		sourceSet.convention.plugins."${name}" = getSourceSetConvention(sourceSet.displayName, project.fileResolver)

		def mySource = sourceSet."${name}"
		mySource.srcDir { project.file("src/${sourceSet.name}/${name}")}
		sourceSet.allJava.add(mySource.matching(sourceSet.java.filter))
		sourceSet.allSource.add(mySource)
		sourceSet.resources.filter.exclude { FileTreeElement element -> mySource.contains(element.file) }
		
		String taskName = sourceSet.getCompileTaskName(name)
		AbstractCompile compileTask = project.tasks.add(taskName, getCompileTaskClass())
		compileTask.dependsOn sourceSet.compileJavaTaskName
		javaPlugin.configureForSourceSet(sourceSet, compileTask);
		compileTask.description = "Compiles the ${sourceSet.name} ${name} files.";
    compileTask.conventionMapping.defaultSource = { mySource }
		project.tasks[sourceSet.classesTaskName].dependsOn(taskName)
	}

	protected void configureCompilerConfigurations(Project project) {
		getCompileConfigurationNames()?.each { String configName ->
			def config = project.configurations.add(configName).setVisible(false).setTransitive(true)
			configureCompilerConfiguration(config, project)
		}
	}

	protected void configureCompilerConfiguration(Configuration config, Project project) {
		project.tasks.withType(getCompileTaskClass()) { AbstractCompile compile ->
			configureCompilerConfiguration(config, compile, project)
		}

		if(hasDoc()) {
			project.tasks.withType(getDocTaskClass()) { AbstractDoc doc ->
				configureCompilerConfiguration(config, doc, project)
			}
		}
	}

	protected void configureCompilerConfiguration(Configuration config, AbstractDoc docTask, Project project) {
		docTask.conventionMapping.destinationDir = { project.file("${project.docsDir}/${getName()}Doc") }
		docTask.conventionMapping.title = { project.apiDocTitle }
		docTask.localClasspath = config
	}

	protected void configureCompilerConfiguration(Configuration config, AbstractCompile compileTask, Project project) {
		compileTask.localClasspath = config
	}
			
}