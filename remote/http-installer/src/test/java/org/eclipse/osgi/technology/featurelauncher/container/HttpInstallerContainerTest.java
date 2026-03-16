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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;

class HttpInstallerContainerTest {

	static final String IMAGE = "feature-launcher-remote-http-installer:1.0.0-SNAPSHOT";

	static GenericContainer<?> createContainer() {
		return new GenericContainer<>(IMAGE)
				.waitingFor(Wait.forLogMessage(".*Launching OSGi Framework\\.\\.\\..*\\n", 1))
				.withStartupTimeout(Duration.ofSeconds(60));
	}

	@Nested
	@org.testcontainers.junit.jupiter.Testcontainers
	class OnceMode {

		@Container
		GenericContainer<?> container = createContainer()
				.withEnv("OSGITECH_HTTP_FEATURES_URL", "http://example.com/features")
				.withEnv("OSGITECH_HTTP_SCAN_MODE", "ONCE");

		@Test
		void shouldLogBanner() {
			String logs = container.getLogs();
			assertThat(logs).contains("OSGi Feature Launcher (HTTP Installer)");
			assertThat(logs).contains("Launching OSGi Framework");
		}

		@Test
		void shouldUseScanModeOnce() {
			String logs = container.getLogs();
			assertThat(logs).contains("Scan Mode:     ONCE");
		}

		@Test
		void shouldConfigureFeaturesUrl() {
			String logs = container.getLogs();
			assertThat(logs).contains("Features URL:  http://example.com/features");
		}

		@Test
		void shouldLogDefaultScanInterval() {
			String logs = container.getLogs();
			assertThat(logs).contains("Scan Interval: 300");
		}
	}

	@Nested
	@org.testcontainers.junit.jupiter.Testcontainers
	class WatchMode {

		@Container
		GenericContainer<?> container = createContainer()
				.withEnv("OSGITECH_HTTP_FEATURES_URL", "http://example.com/features")
				.withEnv("OSGITECH_HTTP_SCAN_MODE", "WATCH").withEnv("OSGITECH_HTTP_SCAN_INTERVAL", "5");

		@Test
		void shouldUseScanModeWatch() {
			String logs = container.getLogs();
			assertThat(logs).contains("Scan Mode:     WATCH");
		}

		@Test
		void shouldUseCustomScanInterval() {
			String logs = container.getLogs();
			assertThat(logs).contains("Scan Interval: 5");
		}
	}

	@Nested
	@org.testcontainers.junit.jupiter.Testcontainers
	class WithServerId {

		@Container
		GenericContainer<?> container = createContainer()
				.withEnv("OSGITECH_HTTP_FEATURES_URL", "http://example.com/features?node={serverId}")
				.withEnv("OSGITECH_HTTP_SERVER_ID", "my-node-1");

		@Test
		void shouldLogServerId() {
			String logs = container.getLogs();
			assertThat(logs).contains("Server ID:     my-node-1");
		}

		@Test
		void shouldPassServerIdUrlPlaceholder() {
			String logs = container.getLogs();
			assertThat(logs).contains("Features URL:  http://example.com/features?node={serverId}");
		}
	}

	@Nested
	class WithFeatureServer {

		@Test
		void shouldPointToFeatureServer() throws IOException {
			String featureJson = Files.readString(Path.of("src/test/resources/features/test-feature.json"));

			try (FeatureServer featureServer = new FeatureServer()) {
				featureServer.setFeatureJson(featureJson);
				Testcontainers.exposeHostPorts(featureServer.getPort());

				String featuresUrl = featureServer.getUrl();

				try (GenericContainer<?> container = createContainer().withEnv("OSGITECH_HTTP_FEATURES_URL",
						featuresUrl)) {
					container.start();

					String logs = container.getLogs();
					assertThat(logs).contains("Features URL:  " + featuresUrl);
					assertThat(logs).contains("Launching OSGi Framework");
				}
			}
		}
	}
}
