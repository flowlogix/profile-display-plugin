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

import org.apache.maven.execution.MavenSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.Properties;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProfileDisplayExtensionTest {
    private final MavenSession session = mock(MavenSession.class);
    private final Properties userProperties = new Properties();
    private final Properties systemProperties = new Properties();
    private final ProfileDisplayExtension extension = new ProfileDisplayExtension();

    @Test
    void disabledByDefault() {
        run();
        verify(session, never()).getTopLevelProject();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "true"})
    void enabledByProperty(String value) {
        userProperties.setProperty("profile.display", value);
        run();
        verify(session).getTopLevelProject();
    }

    private void run() {
        when(session.getUserProperties()).thenReturn(userProperties);
        when(session.getSystemProperties()).thenReturn(systemProperties);
        extension.afterProjectsRead(session);
    }
}
