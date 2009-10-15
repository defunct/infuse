package com.goodworkalan.infuse.jpa.mix;

import com.goodworkalan.go.go.Artifact;
import com.goodworkalan.mix.BasicJavaModule;

public class InfuseJpaModule extends BasicJavaModule {
    public InfuseJpaModule() {
        super(new Artifact("com.goodworkalan", "infuse-jpa", "0.1"));
        addDependency(new Artifact("com.goodworkalan", "infuse", "0.1"));
        addDependency(new Artifact("org.hibernate", "hibernate-core", "3.3.1.GA"));
        addDependency(new Artifact("org.hibernate", "hibernate-annotations", "3.4.0.GA"));
        addTestDependency(new Artifact("org.slf4j", "slf4j-log4j12", "1.4.2"));
        addTestDependency(new Artifact("log4j", "log4j", "1.2.14"));
        addTestDependency(new Artifact("org.testng", "testng", "5.10"));
        addTestDependency(new Artifact("org.mockito", "mockito-core", "1.6"));
    }
}
