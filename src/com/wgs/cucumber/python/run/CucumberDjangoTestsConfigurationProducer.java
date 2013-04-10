package com.wgs.cucumber.python.run;

import com.intellij.execution.Location;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.django.testRunner.DjangoTestsConfigurationProducer;
import com.jetbrains.django.testRunner.DjangoTestsRunConfiguration;
import com.jetbrains.python.buildout.BuildoutFacet;
import com.jetbrains.python.buildout.config.psi.BuildoutPsiUtil;
import com.jetbrains.python.buildout.config.psi.impl.BuildoutCfgSection;
import com.jetbrains.python.psi.PyUtil;
import org.jetbrains.plugins.cucumber.psi.GherkinPsiElement;
import org.jetbrains.plugins.cucumber.psi.GherkinScenario;

/**
 * Created with IntelliJ IDEA.
 * User: emmanuel
 * Date: 10/04/13
 * Time: 18:04
 * To change this template use File | Settings | File Templates.
 */
public class CucumberDjangoTestsConfigurationProducer extends DjangoTestsConfigurationProducer {

    @Override
    protected RunnerAndConfigurationSettings createConfigurationByElement(Location location, ConfigurationContext context) {

        PsiElement element = location.getPsiElement();
        GherkinPsiElement gherkinPsiElement = PsiTreeUtil.getParentOfType(element, GherkinPsiElement.class);
        if (gherkinPsiElement != null) {
            return createConfigurationByGherkinElement(gherkinPsiElement, context);
        } else {
            return null;
        }
    }

    private GherkinScenario getScenarioFromElement(PsiElement element) {
        return PsiTreeUtil.getParentOfType(element, GherkinScenario.class);
    }

    private int getScenarioIndex(GherkinScenario scenario) {
        int scenarioIndex = 0;
        GherkinScenario currentScenario = PsiTreeUtil.getPrevSiblingOfType(scenario, GherkinScenario.class);
        while (currentScenario != null) {
            currentScenario = PsiTreeUtil.getPrevSiblingOfType(currentScenario, GherkinScenario.class);
            scenarioIndex++;
        }

        return scenarioIndex;
    }

    private String computeFeatureClassName(String featureFileName) {
        String[] featureComponents = featureFileName.split("\\.");

        return featureComponents[0];
    }

    private RunnerAndConfigurationSettings createConfigurationByGherkinElement(GherkinPsiElement element, ConfigurationContext context) {
        String appName = "wgsportal";


        Module module = ModuleUtil.findModuleForPsiElement(element);
        //GherkinScenario scenario = getScenarioFromElement(element);

        PsiFile featureFileElement = PsiTreeUtil.getParentOfType(element, PsiFile.class);
        RunnerAndConfigurationSettings result = null;
        if (featureFileElement != null) {

            result = cloneTemplateConfiguration(context.getProject(), context);

            String target = computeTargetForGherkinElement(featureFileElement, element);


            DjangoTestsRunConfiguration configuration = (DjangoTestsRunConfiguration) result.getConfiguration();

            configuration.setName((new StringBuilder()).append("Cucumber: ").append(target).toString());
            configuration.setUseModuleSdk(true);
            configuration.setModule(module);
            configuration.setTarget(target);
            setDefaultSettingsFile(configuration, appName);
        }
        return result;
    }

    private String computeTargetForGherkinElement(PsiFile featureFile, GherkinPsiElement gherkinPsiElement) {
        StringBuilder builder = new StringBuilder();

        builder.append("bdd.").append(computeFeatureClassName(featureFile.getName()));

        if (gherkinPsiElement instanceof GherkinScenario) {
            builder.append(".test_scenario_").append(getScenarioIndex((GherkinScenario) gherkinPsiElement));
        }

        return builder.toString();
    }

    /*@Override
    public RunnerAndConfigurationSettings createConfiguration(Module module, String appName, ConfigurationContext context, PsiElement psiElement) {
        RunnerAndConfigurationSettings result = super.createConfiguration(module, appName, context, psiElement);

        result.setName((new StringBuilder()).append("Cucumber: ").append(result.getName()).toString());
        return result;
    } */
    /*
    @Override
    public RunnerAndConfigurationSettings createConfiguration(Module module, String appName, ConfigurationContext context, PsiElement psiElement) {

        return result;
    }
    */
    private static void setDefaultSettingsFile(DjangoTestsRunConfiguration configuration, String appName) {
        BuildoutFacet buildoutFacet = BuildoutFacet.getInstance(configuration.getModule());
        if (buildoutFacet != null) {
            com.jetbrains.python.buildout.config.psi.impl.BuildoutCfgFile configFile = buildoutFacet.getConfigPsiFile();
            if (configFile != null) {
                BuildoutCfgSection section = BuildoutPsiUtil.getDjangoSection(configFile);
                if (section != null && appName.equals(section.getOptionValue("test"))) {
                    String projectName = section.getOptionValue("project");
                    String settingsFileName = section.getOptionValue("settings");
                    if (projectName != null && settingsFileName != null) {
                        VirtualFile settingsFile = PyUtil.findInRoots(configuration.getModule(), (new StringBuilder()).append(projectName).append("/").append(settingsFileName).append(".py").toString());
                        if (settingsFile != null)
                            configuration.setSettingsFile(settingsFile.getPath());
                    }
                }
            }
        }
    }

    //public RunnerAndConfigurationSettings createConfiguration(Module module, String appName, ConfigurationContext context, PsiElement psiElement) {

 /*   private PsiElement mySourceElement;

    public CucumberDjangoTestsConfigurationProducer() {
        super(DjangoTestsConfigurationType.getInstance());
    }

    @Override
    public int compareTo(final Object o) {
        return PREFERED;
    }

    @Override
    public PsiElement getSourceElement() {
        return mySourceElement;
    }

    @Nullable
    @Override
    protected RunnerAndConfigurationSettings createConfigurationByElement(Location location, ConfigurationContext configurationcontext) {
        mySourceElement = location.getPsiElement();


        return null;
    }*/
    /*
    protected RunnerAndConfigurationSettings createConfigurationByElement(Location location, ConfigurationContext context) {
        String appName = DjangoTestUtil.getAppNameForLocation(location.getModule(), location.getPsiElement());
        if (appName != null) {
            myPsiElement = location.getPsiElement();
            Module module = ModuleUtil.findModuleForPsiElement(myPsiElement);
            if (module == null)
                return null;
            else
                return createConfiguration(module, appName, context, location.getPsiElement());
        } else {
            return null;
        }
    }*/

  /*  public CucumberDjangoTestsConfigurationProducer() {
        super(DjangoTestsConfigurationType.getInstance());
    }

    public static CucumberDjangoTestsConfigurationProducer getInstance() {
        return getInstance(CucumberDjangoTestsConfigurationProducer.class);
    }

    public PsiElement getSourceElement() {
        return myPsiElement;
    }

    protected RunnerAndConfigurationSettings createConfigurationByElement(Location location, ConfigurationContext context) {
        String appName = DjangoTestUtil.getAppNameForLocation(location.getModule(), location.getPsiElement());
        if (appName != null) {
            myPsiElement = location.getPsiElement();
            Module module = ModuleUtil.findModuleForPsiElement(myPsiElement);
            if (module == null)
                return null;
            else
                return createConfiguration(module, appName, context, location.getPsiElement());
        } else {
            return null;
        }
    }

    public RunnerAndConfigurationSettings createConfiguration(Module module, String appName, ConfigurationContext context, PsiElement psiElement) {
        RunnerAndConfigurationSettings result = cloneTemplateConfiguration(module.getProject(), context);
        String target = DjangoTestUtil.buildTargetFromLocation(appName, psiElement);
        DjangoTestsRunConfiguration configuration = (DjangoTestsRunConfiguration) result.getConfiguration();
        configuration.setName((new StringBuilder()).append("Test: ").append(target).toString());
        configuration.setUseModuleSdk(true);
        configuration.setModule(module);
        configuration.setTarget(target);
        setDefaultSettingsFile(configuration, appName);
        return result;
    }

    private static void setDefaultSettingsFile(DjangoTestsRunConfiguration configuration, String appName) {
        BuildoutFacet buildoutFacet = BuildoutFacet.getInstance(configuration.getModule());
        if (buildoutFacet != null) {
            com.jetbrains.python.buildout.config.psi.impl.BuildoutCfgFile configFile = buildoutFacet.getConfigPsiFile();
            if (configFile != null) {
                BuildoutCfgSection section = BuildoutPsiUtil.getDjangoSection(configFile);
                if (section != null && appName.equals(section.getOptionValue("test"))) {
                    String projectName = section.getOptionValue("project");
                    String settingsFileName = section.getOptionValue("settings");
                    if (projectName != null && settingsFileName != null) {
                        VirtualFile settingsFile = PyUtil.findInRoots(configuration.getModule(), (new StringBuilder()).append(projectName).append("/").append(settingsFileName).append(".py").toString());
                        if (settingsFile != null)
                            configuration.setSettingsFile(settingsFile.getPath());
                    }
                }
            }
        }
    }

    @Override
    protected RunnerAndConfigurationSettings findExistingByElement(Location location, @NotNull RunnerAndConfigurationSettings existingConfigurations[], ConfigurationContext context) {

        String appName = DjangoTestUtil.getAppNameForLocation(location.getModule(), location.getPsiElement());
        String targetFromLocation = DjangoTestUtil.buildTargetFromLocation(appName, location.getPsiElement());

        for (RunnerAndConfigurationSettings existingConfiguration : existingConfigurations) {
            RunConfiguration configuration = existingConfiguration.getConfiguration();
            if ((configuration instanceof DjangoTestsRunConfiguration) && Comparing.equal(targetFromLocation, ((DjangoTestsRunConfiguration) configuration).getTarget()))
                return existingConfiguration;
        }

        return null;
    }

    public int compareTo(Object o) {
        return -1;
    }

    private PsiElement myPsiElement;    */

}
