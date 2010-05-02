package com.goodworkalan.infuse.mix;

import com.goodworkalan.go.go.Artifact;
import com.goodworkalan.mix.ProjectModule;
import com.goodworkalan.mix.builder.Builder;
import com.goodworkalan.mix.builder.JavaProject;

public class InfuseProject extends ProjectModule {
    @Override
    public void build(Builder builder) {
        builder
            .cookbook(JavaProject.class)
                .produces(new Artifact("com.goodworkalan/infuse/0.1"))
                .main()
                    .depends()
                        .artifact(new Artifact("com.github.bigeasy.danger/danger/0.1"))
                        .artifact(new Artifact("com.github.bigeasy.class-boxer/class-boxer/0.1"))
                        .artifact(new Artifact("com.github.bigeasy.class-association/class-association/0.1"))
                        .artifact(new Artifact("com.goodworkalan/reflective/0.1"))
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
