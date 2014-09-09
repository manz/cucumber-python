package com.wgs.cucumber.python.run;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ModuleBasedConfiguration;
import com.intellij.openapi.project.Project;
import com.jetbrains.django.testRunner.DjangoTestsRunConfiguration;

public class CucumberDjangoTestsRunConfiguration extends DjangoTestsRunConfiguration {
    public CucumberDjangoTestsRunConfiguration(Project project, ConfigurationFactory factory) {
        super(project, factory);
    }

    protected ModuleBasedConfiguration createInstance()
    {
        return new CucumberDjangoTestsRunConfiguration(getProject(), getFactory());
    }


}


