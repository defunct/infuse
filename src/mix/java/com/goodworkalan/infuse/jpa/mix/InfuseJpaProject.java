package com.goodworkalan.infuse.jpa.mix;

import com.goodworkalan.go.go.Artifact;
import com.goodworkalan.mix.ProjectModule;
import com.goodworkalan.mix.builder.Builder;
import com.goodworkalan.mix.builder.JavaProject;

public class InfuseJpaProject extends ProjectModule {
    @Override
    public void build(Builder builder) {
        builder
            .cookbook(JavaProject.class)
                .produces(new Artifact("com.goodworkalan/infuse-jpa/0.1"))
                .main()
                    .depends()
                        .artifact(new Artifact("com.goodworkalan/infuse/0.1"))
                        .artifact(new Artifact("org.hibernate/hibernate-core/3.3.1.GA"))
                        .artifact(new Artifact("org.hibernate/hibernate-annotations/3.4.0.GA"))
                        .end()
                    .end()
                .test()
                    .depends()
                        .artifact(new Artifact("org.testng/testng/5.10"))
                        .artifact(new Artifact("org.mockito/mockito-core/1.6"))
                        .end()
                    .end()
                .end()
            .end();
    }
}
