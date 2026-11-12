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

import lombok.Value;
import org.apache.maven.model.Profile;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.jspecify.annotations.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Gathers profiles declared in the project's pom hierarchy and in settings,
 * flagging the ones active for the current build.
 */
final class ProfileCollector {
    static final String SOURCE_POM = "pom";
    static final String SOURCE_SETTINGS = "settings.xml";

    private ProfileCollector() { }

    /**
     * Profile metadata for display.
     */
    @Value
    static class ProfileInfo {
        /**
         * Profile id.
         */
        String id;
        /**
         * Where the profile is declared, either the pom chain or settings.
         */
        String source;
        /**
         * Whether the profile is active for this build.
         */
        boolean active;
    }

    /**
     * Collects profiles for a project and its parents, then from settings.
     * Profiles are returned in declaration order, nearest pom first, with duplicate ids
     * only reported once (first declaration wins).
     *
     * @param project project to inspect, usually the top-level project in the reactor
     * @param settings effective settings, may be null when unavailable
     * @return profiles in display order
     */
    static List<ProfileInfo> collect(MavenProject project, @Nullable Settings settings) {
        Set<String> activeIds = project.getActiveProfiles().stream()
                .map(Profile::getId).collect(Collectors.toSet());
        Stream<ProfileInfo> pomProfiles = Stream.iterate(project, Objects::nonNull, MavenProject::getParent)
                .flatMap(current -> current.getModel().getProfiles().stream())
                .map(profile -> new ProfileInfo(profile.getId(), SOURCE_POM, activeIds.contains(profile.getId())));
        Stream<ProfileInfo> settingsProfiles = Optional.ofNullable(settings)
                .map(Settings::getProfiles).stream().flatMap(List::stream)
                .map(profile -> new ProfileInfo(profile.getId(), SOURCE_SETTINGS, activeIds.contains(profile.getId())));
        return Stream.concat(pomProfiles, settingsProfiles)
                .filter(distinctById())
                .collect(Collectors.toList());
    }

    private static Predicate<ProfileInfo> distinctById() {
        Set<String> seen = new HashSet<>();
        return profile -> seen.add(profile.getId());
    }
}
