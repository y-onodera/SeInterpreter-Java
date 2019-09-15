package com.sebuilder.interpreter;

public interface StepTypeFactory {
    void setPrimaryPackage(String primaryPackage);

    void setSecondaryPackage(String secondaryPackage);

    StepType getStepTypeOfName(String name);
}
