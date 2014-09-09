/*
 * Copyright 2013 Web Geo Services
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wgs.cucumber.python;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.util.PathUtil;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.python.PythonFileType;
import com.jetbrains.python.psi.PyDecoratorList;
import com.jetbrains.python.psi.PyFile;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.psi.PyStatement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.CucumberJvmExtensionPoint;
import org.jetbrains.plugins.cucumber.StepDefinitionCreator;
import org.jetbrains.plugins.cucumber.psi.GherkinFile;
import org.jetbrains.plugins.cucumber.psi.GherkinRecursiveElementVisitor;
import org.jetbrains.plugins.cucumber.psi.GherkinStep;
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition;
import org.jetbrains.plugins.cucumber.steps.CucumberStepsIndex;

import java.util.*;

/**
 * @author Emmanuel Peralta
 */
public class CucumberPythonExtension implements CucumberJvmExtensionPoint {

    @Override
    public boolean isStepLikeFile(@NotNull PsiElement child, @NotNull PsiElement parent) {
        return child instanceof PyFile && ((PyFile) child).getName().endsWith(".py");
    }

    @Override
    public boolean isWritableStepLikeFile(@NotNull PsiElement child, @NotNull PsiElement parent) {
        return isStepLikeFile(child, parent);
    }

    @NotNull
    @Override
    public FileType getStepFileType() {
        return PythonFileType.INSTANCE;
    }

    @NotNull
    @Override
    public StepDefinitionCreator getStepDefinitionCreator() {
        return new PyStepDefinitionCreator();
    }

    @Override
    public List<PsiElement> resolveStep(@NotNull PsiElement psiElement) {
        final CucumberStepsIndex index = CucumberStepsIndex.getInstance(psiElement.getProject());

        if (psiElement instanceof GherkinStep) {
            final GherkinStep step = (GherkinStep) psiElement;
            final List<PsiElement> result = new ArrayList<PsiElement>();
            final Set<String> substitutedNameList = step.getSubstitutedNameList();
            if (substitutedNameList.size() > 0) {
                for (String s : substitutedNameList) {
                    final AbstractStepDefinition definition = index.findStepDefinition(psiElement.getContainingFile(), s);
                    if (definition != null) {
                        result.add(definition.getElement());
                    }
                }
                return result;
            }
        }

        return Collections.emptyList();
    }



 /*   @Override
    public void findRelatedStepDefsRoots(Module module, PsiFile featureFile, List<PsiDirectory> newStepDefinitionsRoots, Set<String> processedStepDirectories) {
        PsiDirectory parent = PsiTreeUtil.getParentOfType(featureFile, PsiDirectory.class);

        PsiDirectory steps = null;
        if (parent != null) {
            steps = parent.findSubdirectory("steps");
        }

        VirtualFile virtualStepsDir = null;

        if (steps != null) {
            virtualStepsDir = steps.getVirtualFile();
        }

        if (virtualStepsDir != null && virtualStepsDir.isDirectory()) {
            PsiDirectory sourceRoot = PsiDirectoryFactory.getInstance(module.getProject()).createDirectory(virtualStepsDir);
            if (!processedStepDirectories.contains(sourceRoot.getVirtualFile().getPath())) {
                newStepDefinitionsRoots.add(sourceRoot);
            }
        }
    }*/

    @Nullable
    public String getGlue(@NotNull GherkinStep step) {
        for (PsiReference ref : step.getReferences()) {
            PsiElement refElement = ref.resolve();

            if (refElement != null) {
                PyFile pyFile = (PyFile) refElement.getContainingFile();
                VirtualFile virtualFile = pyFile.getVirtualFile();
                if (virtualFile != null) {
                    VirtualFile parentDir = virtualFile.getParent();
                    return PathUtil.getLocalPath(parentDir);
                }
            }

        }
        return null;
    }


    @NotNull
    @Override
    public Collection<String> getGlues(@NotNull GherkinFile gherkinFile, Set<String> strings) {
        final Set<String> glues = ContainerUtil.newHashSet();

        gherkinFile.accept(new GherkinRecursiveElementVisitor() {
            @Override
            public void visitStep(GherkinStep step) {
                final String glue = getGlue(step);
                if (glue != null) {
                    glues.add(glue);
                }
            }
        });
        return glues;

    }


    /** New methods **/
    @Override
    public List<AbstractStepDefinition> loadStepsFor(@Nullable PsiFile psiFile, @NotNull Module module) {
        List<AbstractStepDefinition> newDefs = new ArrayList<AbstractStepDefinition>();

        if (psiFile != null) {
            PsiDirectory[] directories = psiFile.getContainingDirectory().getSubdirectories();

            for (PsiDirectory directory : directories) {
                if (directory.getName().equals("steps")) {
                    PsiFile[] stepFiles = directory.getFiles();
                    for (PsiFile stepFile : stepFiles) {
                        if (stepFile instanceof PyFile) {
                            PyFile pyFile = (PyFile)stepFile;

                            List<PyStatement> statements = pyFile.getStatements();

                            for (PyStatement statement : statements) {
                                if (statement instanceof PyFunction) {
                                    PyFunction func = (PyFunction) statement;
                                    PyDecoratorList decorators = func.getDecoratorList();

                                    if (decorators != null) {
                                        LettuceStepDefinition stepWannabe = new LettuceStepDefinition(func);

                                        if (stepWannabe.getCucumberRegex() != null) {
                                            newDefs.add(stepWannabe);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }
        return newDefs;
    }

    @Override
    public void flush(@NotNull Project project) {

    }

    @Override
    public void reset(@NotNull Project project) {

    }

    @Override
    public Object getDataObject(@NotNull Project project) {
        return null;
    }

    @Override
    public Collection<? extends PsiFile> getStepDefinitionContainers(@NotNull GherkinFile gherkinFile) {
        return null;
    }
}
