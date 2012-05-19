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
import static com.smokejumperit.gradle.compilers.GeneratorPluginHelper.*;

public abstract class GeneratorPlugin<GENERATOR_TYPE extends AbstractGenerator> 
	extends CompilerPlugin<GENERATOR_TYPE> implements Plugin<Project> 
{

	@Override 
	public void apply(Project project) {
		project.getTasks().withType(AbstractGenerator.class).all(getAttachCopyAction()); 
		super.apply(project);
	}

	protected Action<AbstractGenerator> getAttachCopyAction() {
		return new Action<AbstractGenerator>() {
			public void execute(final AbstractGenerator generator) {
				generator.getLogger().info("Adding the copy action to " + generator);
				generator.doLast(getExecuteCopyAction(generator));
			}
		};
	}

	protected <T extends Task> Action<T> getExecuteCopyAction(final AbstractGenerator generator) {
		return new Action<T>() {
			public void execute(T task) {
				copyGeneratedFiles(
					generator.getProject(), getAdditionalConsumedFileSuffixes(), 
					generator.getSource(), generator.getDestinationDir()
				);
			}
		};
	}

}
