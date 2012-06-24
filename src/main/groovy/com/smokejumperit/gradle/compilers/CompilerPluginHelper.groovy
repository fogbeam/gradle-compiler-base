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

class CompilerPluginHelper {

	/**
	* Assigns the configuration methods to the CompilerSourceSet and then assigns the CompilerSourceSet to the SourceSet
	* as the convention object.
	*/
	static CompilerSourceSet applyConvention(SourceSet ss, String name, CompilerSourceSet css) {
		css.metaClass."$name" = { Closure c ->
			ConfigureUtil.configure(c, css)
		}

		def upName = "${name[0].toUpperCase()}${name[1..-1]}"

		css.metaClass."get${upName}" = {-> 
			css.compilerSourceDirSet
		}
		css.metaClass."getAll${upName}" = {->
			css.allSourceDirSet
		}

		ss.convention.plugins."$name" = css

		return css
	}

}
