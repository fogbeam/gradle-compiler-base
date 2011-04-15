package com.smokejumperit.gradle.compilers;

import org.gradle.api.tasks.compile.AbstractCompile
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileTreeElement
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.internal.file.SourceDirectorySet
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import org.gradle.api.file.FileTree;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.internal.file.DefaultSourceDirectorySet;
import org.gradle.api.internal.file.FileResolver;
import org.gradle.api.internal.file.UnionFileTree;
import org.gradle.api.tasks.ScalaSourceSet;
import org.gradle.api.tasks.util.PatternFilterable;
import org.gradle.api.tasks.util.PatternSet;
import org.gradle.util.ConfigureUtil;
import org.gradle.api.artifacts.Configuration


public abstract class CompilerPlugin implements Plugin<Project> {

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
	protected abstract <X extends AbstractCompile> Class<X> getCompileTaskClass();

	/**
	* Provides the documentation class task name. May be <code>null</code> if there is no documentation class.
	*/
	protected abstract <X/* extends AbstractDoc*/> Class<X> getDocTaskClass();

	/**
	* Extra configurations to be added to the compiler classpath. Return <code>null</code> or an empty list if none.
	*/
	protected abstract Collection<String> getCompileConfigurationNames();

	/**
	* Method to override if you want to configure the doc task for a given project.
	*/
	protected void configureDoc(/*AbstractDoc*/ doc, Project project) {}

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

		configureSourceSetsDefaults(project, javaPlugin)
		if(hasDoc()) addDocTask(project)
		configureCompilerConfigurations(project)
	}

	protected void addDocTask(Project project) {
		project.tasks.withType(getDocTaskClass()) { /*AbstractDoc*/ doc ->
			doc.conventionMapping.classpath = { project.sourceSets.main.classes + project.sourceSets.main.compileClasspath }
			doc.conventionMapping.defaultSource = { project.sourceSets.main."${getName()}" }
			configureDoc(doc, project)
		}
		def /*AbstractDoc*/ doc = project.tasks.add("${getName()}doc", getDocTaskClass())
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
		applySourceSetConvention(sourceSet.displayName, project.fileResolver, sourceSet)

		String name = getName()
		def mySource = sourceSet."${name}"
		mySource.srcDir { project.file("src/${sourceSet.name}/${name}") }
		
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
			project.tasks.withType(getDocTaskClass()) { /*AbstractDoc*/ doc ->
				configureCompilerConfiguration(config, doc, project)
			}
		}
	}

	protected void configureCompilerConfiguration(Configuration config, /*AbstractDoc*/ docTask, Project project) {
		docTask.conventionMapping.destinationDir = { project.file("${project.docsDir}/${getName()}Doc") }
		docTask.conventionMapping.title = { project.apiDocTitle }
		docTask.classpath.add(config)
	}

	protected void configureCompilerConfiguration(Configuration config, AbstractCompile compileTask, Project project) {
		compileTask.classpath.add(config)
	}
			
	/**
	* Applies the source set convention to the source set
	*/
	protected def applySourceSetConvention(String displayName, FileResolver resolver, SourceSet sourceSet) {
		String name = getName()
		String capitalName = "${name[0].toUpperCase()}${name[1..-1]}"

		def langSuffixes = (languageFileSuffixes ?: [])
		def allSuffixes = []
		allSuffixes.addAll(langSuffixes)
		allSuffixes.addAll(getAdditionalConsumedFileSuffixes() ?: [])

		DefaultSourceDirectorySet files = new DefaultSourceDirectorySet("$displayName $capitalName source", resolver)
		files.filter.include(allSuffixes.collect { "**/*.${it}" })

		PatternFilterable langPatterns = new PatternSet()
		langPatterns.include(langSuffixes.collect { "**/*.${it}" })
		UnionFileTree allFiles = new UnionFileTree("$displayName $capitalName source", files.matching(langPatterns))

		def convention = sourceSet
		
		convention.metaClass."get${capitalName}" = {->
			return files
		}

		convention.metaClass."${name}" = { Closure c -> 
			ConfigureUtil.configure(c, files)
			return this;
		}

		convention.metaClass."get${capitalName}SourcePatterns" = {->
			return langPatterns
		}

		convention.metaClass."getAll${capitalName}" = {->
			return allFiles
		}

		return convention
	}
}
