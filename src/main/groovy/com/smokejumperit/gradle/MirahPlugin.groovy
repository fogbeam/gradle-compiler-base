package com.smokejumperit.gradle;

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileTreeElement
import org.gradle.api.internal.tasks.DefaultScalaSourceSet
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import com.smokejumperit.gradle.mirah.*
import com.smokejumperit.gradle.compilers.CompilerPlugin

public class MirahPlugin extends CompilerPlugin implements Plugin<Project> {

  /**
  * The file suffixes specific to the language of this compiler. 
  */
  protected Collection<String> getLanguageFileSuffixes() {
		return ["mirah"]
	}

  /**
  * Additional file suffixes consumed by the compiler but not unique to it. For example, both Groovy and Scala also consume 
  * <code>*.java</code> files. May return either <code>null</code> or an empty list to denote that only language file suffixes
  * are consumed.
  */
  protected Collection<String> getAdditionalConsumedFileSuffixes() { return Collections.emptyList() }

	protected String getName() { return "mirah"; }

	protected Class<MirahCompile> getCompileTaskClass() { return MirahCompile.class; }

	/**
	* Returns <code>null</code>: no documentation!
	*/
	protected Class<?/* extends AbstractDoc*/> getDocTaskClass() { return null; }

	/**
	* Provides the necessary compile configuration names for Mirah.
	*/
	protected Collection<String> getCompileConfigurationNames() { return Collections.emptyList(); }


}
