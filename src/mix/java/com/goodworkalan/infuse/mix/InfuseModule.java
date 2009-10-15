package com.goodworkalan.infuse.mix;

import com.goodworkalan.go.go.Artifact;
import com.goodworkalan.mix.BasicJavaModule;

public class InfuseModule extends BasicJavaModule {
    public InfuseModule() {
        super(new Artifact("com.goodworkalan", "infuse", "0.1"));
        addDependency(new Artifact("com.goodworkalan", "reflective", "0.1"));
        addTestDependency(new Artifact("org.testng", "testng", "5.10"));
        addTestDependency(new Artifact("org.mockito", "mockito-core", "1.6"));
    }
}
