/*********************************************************************
* Copyright (c) 2026 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.osgi.technology.featurelauncher.container;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class RemoteBaseContainerTest {

	static final String IMAGE = "feature-launcher-remote-base:1.0.0-SNAPSHOT";

	static GenericContainer<?> createContainer() {
		return new GenericContainer<>(IMAGE)
				.waitingFor(Wait.forLogMessage(".*Launching OSGi Framework\\.\\.\\..*\\n", 1))
				.withStartupTimeout(Duration.ofSeconds(60));
	}

	@Container
	GenericContainer<?> container = createContainer();

	@Test
	void shouldLogBanner() {
		String logs = container.getLogs();
		assertThat(logs).contains("OSGi Feature Launcher");
		assertThat(logs).contains("Launching OSGi Framework");
	}

	@Test
	void shouldLogBootstrapPath() {
		String logs = container.getLogs();
		assertThat(logs).contains("Bootstrap: /app/bootstrap/bootstrap.json");
	}
}
