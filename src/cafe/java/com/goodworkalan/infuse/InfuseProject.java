package com.goodworkalan.infuse;

import com.goodworkalan.cafe.ProjectModule;
import com.goodworkalan.cafe.builder.Builder;
import com.goodworkalan.cafe.outline.JavaProject;

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
                .produces("com.github.bigeasy.infuse/infuse/0.1.1.7")
                .depends()
                    .production("com.github.bigeasy.class/class-boxer/0.+1")
                    .production("com.github.bigeasy.danger/danger/0.+3")
                    .production("com.github.bigeasy.class/class-association/0.+1")
                    .development("org.testng/testng-jdk15/5.10")
                    .development("org.mockito/mockito-core/1.6")
                    .end()
                .end()
            .end();
    }
}
