package com.goodworkalan.infuse.mix;

import com.goodworkalan.mix.ProjectModule;
import com.goodworkalan.mix.builder.Builder;
import com.goodworkalan.mix.builder.JavaProject;

/**
 * Builds the project definition for Infuse.
 *
 * @author Alan Gutierrez
 */
public class InfuseProject implements ProjectModule {
    /**
     * Build the project definition for Infuse.
     *
     * @param builder
     *          The project builder.
     */
    public void build(Builder builder) {
        builder
            .cookbook(JavaProject.class)
                .produces("com.github.bigeasy.infuse/infuse/0.1")
                .depends()
                    .production("com.github.bigeasy.danger/danger/0.+1")
                    .production("com.github.bigeasy.class-boxer/class-boxer/0.+1")
                    .production("com.github.bigeasy.class-association/class-association/0.+1")
                    .production("com.github.bigeasy.reflective/reflective/0.+1")
                    .development("org.testng/testng-jdk15/5.10")
                    .development("org.mockito/mockito-core/1.6")
                    .end()
                .end()
            .end();
    }
}
