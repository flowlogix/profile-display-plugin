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

import com.flowlogix.plugins.profile.ProfileCollector.ProfileInfo;
import org.apache.maven.model.Profile;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.junit.jupiter.api.Test;
import java.util.List;
import static com.flowlogix.plugins.profile.ProfileCollector.SOURCE_POM;
import static com.flowlogix.plugins.profile.ProfileCollector.SOURCE_SETTINGS;
import static org.assertj.core.api.Assertions.assertThat;

class ProfileCollectorTest {
    private final MavenProject project = new MavenProject();

    @Test
    void marksActiveProfiles() {
        project.getModel().addProfile(profile("ci"));
        project.getModel().addProfile(profile("coverage"));
        var settings = new Settings();
        settings.addProfile(settingsProfile("local-repo"));
        project.setActiveProfiles(List.of(profile("coverage"), profile("local-repo")));

        assertThat(ProfileCollector.collect(project, settings)).containsExactly(
                new ProfileInfo("ci", SOURCE_POM, false),
                new ProfileInfo("coverage", SOURCE_POM, true),
                new ProfileInfo("local-repo", SOURCE_SETTINGS, true));
    }

    @Test
    void reportsOverriddenProfileOnceFromNearestPom() {
        var parent = new MavenProject();
        parent.getModel().addProfile(profile("ci"));
        project.setParent(parent);
        project.getModel().addProfile(profile("ci"));
        var settings = new Settings();
        settings.addProfile(settingsProfile("ci"));

        assertThat(ProfileCollector.collect(project, settings))
                .containsExactly(new ProfileInfo("ci", SOURCE_POM, false));
    }

    private static Profile profile(String id) {
        var profile = new Profile();
        profile.setId(id);
        return profile;
    }

    private static org.apache.maven.settings.Profile settingsProfile(String id) {
        var profile = new org.apache.maven.settings.Profile();
        profile.setId(id);
        return profile;
    }
}
