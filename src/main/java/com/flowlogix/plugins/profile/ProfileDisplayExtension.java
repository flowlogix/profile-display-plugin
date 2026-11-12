/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.flowlogix.plugins.profile;

import lombok.extern.slf4j.Slf4j;
import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Maven core extension that lists all profiles of the top-level project once per build,
 * replacing a separate {@code mvn -N help:all-profiles} invocation.
 * <p>
 * Set {@code -Dprofile.display} to enable the output.
 */
@Slf4j
@Named("profile-display")
@Singleton
public class ProfileDisplayExtension extends AbstractMavenLifecycleParticipant {
    /**
     * Property that enables the profile listing.
     */
    public static final String DISPLAY_PROPERTY = "profile.display";

    @Override
    public void afterProjectsRead(MavenSession session) {
        Optional.of(session)
                .filter(ProfileDisplayExtension::isEnabled)
                .map(MavenSession::getTopLevelProject)
                .ifPresent(project -> display(project, session.getSettings()));
    }

    private static void display(MavenProject project, Settings settings) {
        log.info("Listing Profiles for Project: {}", project.getId());
        ProfileCollector.collect(project, settings).forEach(profile ->
                log.info("  Profile Id: {} (Active: {}, Source: {})", profile.getId(), profile.isActive(),
                        profile.getSource()));
    }

    private static boolean isEnabled(MavenSession session) {
        return Stream.of(session.getUserProperties(), session.getSystemProperties())
                .map(properties -> properties.getProperty(DISPLAY_PROPERTY))
                .filter(Objects::nonNull)
                .findFirst()
                .map(value -> value.isEmpty() || Boolean.parseBoolean(value))
                .orElse(false);
    }
}
