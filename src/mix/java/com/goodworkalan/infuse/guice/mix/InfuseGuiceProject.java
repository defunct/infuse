package com.goodworkalan.infuse.guice.mix;

import com.goodworkalan.go.go.Artifact;
import com.goodworkalan.mix.ProjectModule;
import com.goodworkalan.mix.builder.Builder;
import com.goodworkalan.mix.builder.JavaProject;

public class InfuseGuiceProject extends ProjectModule {
    @Override
    public void build(Builder builder) {
        builder
            .cookbook(JavaProject.class)
                .produces(new Artifact("com.goodworkalan/infuse-guice/0.1"))
                .main()
                    .depends()
                        .artifact(new Artifact("com.goodworkalan/infuse/0.1"))
                        .artifact(new Artifact("com.google.inject/guice/2.0"))
                        .end()
                    .end()
                .test()
                    .depends()
                        .artifact(new Artifact("org.testng/testng/5.10/jdk15"))
                        .artifact(new Artifact("org.mockito/mockito-core/1.6"))
                        .end()
                    .end()
                .end()
            .end();
    }
}
