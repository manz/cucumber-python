package com.wgs.cucumber.python.run;

import com.intellij.execution.configurations.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NotNull;

public class CucumberDjangoTestsConfigurationType extends ConfigurationTypeBase {
    public CucumberDjangoTestsConfigurationType() {
        super("CucumberDjangoTestsConfigurationType", "Django Bdd tests", "Django Bdd tests", IconLoader.getIcon("/com/wgs/cucumber/icons/yoshi.png"));
        addFactory(new CucumberDjangoTestsConfigurationFactory(this));
    }

    public static CucumberDjangoTestsConfigurationType getInstance() {
        return ConfigurationTypeUtil.findConfigurationType(CucumberDjangoTestsConfigurationType.class);
    }

    public ConfigurationFactory getDjangoTestsFactory() {
        return getConfigurationFactories()[0];
    }

    private static class CucumberDjangoTestsConfigurationFactory
            extends ConfigurationFactory {

        protected CucumberDjangoTestsConfigurationFactory(@NotNull ConfigurationType type) {
            super(type);
        }

        public RunConfiguration createTemplateConfiguration(Project project) {
            return new CucumberDjangoTestsRunConfiguration(project, this);
        }
    }
}
