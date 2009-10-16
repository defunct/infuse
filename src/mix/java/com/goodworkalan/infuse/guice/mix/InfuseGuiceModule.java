package com.goodworkalan.infuse.guice.mix;

import com.goodworkalan.go.go.Artifact;
import com.goodworkalan.mix.BasicJavaModule;

public class InfuseGuiceModule extends BasicJavaModule {
    public InfuseGuiceModule() {
        super(new Artifact("com.goodworkalan", "infuse-guice", "0.1"));
        addDependency(new Artifact("com.goodworkalan", "infuse", "0.1"));
        addDependency(new Artifact("com.google.inject", "guice", "2.0"));
        addTestDependency(new Artifact("org.testng", "testng", "5.10"));
        addTestDependency(new Artifact("org.mockito", "mockito-core", "1.6"));
    }
}
