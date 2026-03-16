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
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

class DirectoryInstallerContainerTest {

	static final String IMAGE = "feature-launcher-local-directory-installer:1.0.0-SNAPSHOT";

	static GenericContainer<?> createContainer() {
		return new GenericContainer<>(IMAGE)
				.waitingFor(Wait.forLogMessage(".*Using extension handlers:.*\\n", 1))
				.withStartupTimeout(Duration.ofSeconds(60));
	}

	@Nested
	@Testcontainers
	class OnceMode {

		@Container
		GenericContainer<?> container = createContainer().withEnv("OSGITECH_DIR_INSTALLER_SCAN_MODE", "ONCE");

		@Test
		void shouldLogBanner() {
			String logs = container.getLogs();
			assertThat(logs).contains("OSGi Feature Launcher (Directory Installer)");
			assertThat(logs).contains("Launching OSGi Framework");
		}

		@Test
		void shouldUseScanModeOnce() {
			String logs = container.getLogs();
			assertThat(logs).contains("Scan Mode:    ONCE");
			assertThat(logs).contains("installer.directory.scan.mode = ONCE");
		}

		@Test
		void shouldConfigureFeaturesDir() {
			String logs = container.getLogs();
			assertThat(logs).contains("Features Dir: /app/storage/features");
			assertThat(logs).contains("installer.directory.features.dir = /app/storage/features");
		}

		@Test
		void shouldConfigureRepoDir() {
			String logs = container.getLogs();
			assertThat(logs).contains("Repo Dir:     /app/storage/repo");
			assertThat(logs).contains("installer.directory.repo.dir = /app/storage/repo");
		}

		@Test
		void shouldLogDefaultScanInterval() {
			String logs = container.getLogs();
			assertThat(logs).contains("Scan Interval: 300");
		}

		@Test
		void shouldLogContentDir() {
			String logs = container.getLogs();
			assertThat(logs).contains("Content:");
		}

		@Test
		void shouldUseExtensionHandlers() {
			String logs = container.getLogs();
			assertThat(logs).contains("Using extension handlers");
			assertThat(logs).contains("eclipse.osgi.technology.hash.checker");
		}

		@Test
		void shouldLaunchCorrectFeature() {
			String logs = container.getLogs();
			assertThat(logs).contains(
					"Launching feature org.eclipse.osgi.technology.featurelauncher.extras:directory-installer-feature");
		}
	}

	@Nested
	@Testcontainers
	class WatchMode {

		@Container
		GenericContainer<?> container = createContainer().withEnv("OSGITECH_DIR_INSTALLER_SCAN_MODE", "WATCH")
				.withEnv("OSGITECH_DIR_INSTALLER_SCAN_INTERVAL", "5");

		@Test
		void shouldUseScanModeWatch() {
			String logs = container.getLogs();
			assertThat(logs).contains("Scan Mode:    WATCH");
			assertThat(logs).contains("installer.directory.scan.mode = WATCH");
		}

		@Test
		void shouldUseCustomScanInterval() {
			String logs = container.getLogs();
			assertThat(logs).contains("Scan Interval: 5");
		}
	}

	@Nested
	@Testcontainers
	class CustomFeaturesDir {

		@Container
		GenericContainer<?> container = createContainer().withEnv("OSGITECH_DIR_INSTALLER_FEATURES_DIR",
				"/custom/features");

		@Test
		void shouldUseCustomFeaturesDir() {
			String logs = container.getLogs();
			assertThat(logs).contains("Features Dir: /custom/features");
			assertThat(logs).contains("installer.directory.features.dir = /custom/features");
		}
	}

	@Nested
	@Testcontainers
	class CustomRepoDir {

		@Container
		GenericContainer<?> container = createContainer().withEnv("OSGITECH_DIR_INSTALLER_REPO_DIR", "/custom/repo");

		@Test
		void shouldUseCustomRepoDir() {
			String logs = container.getLogs();
			assertThat(logs).contains("Repo Dir:     /custom/repo");
			assertThat(logs).contains("installer.directory.repo.dir = /custom/repo");
		}
	}

	@Nested
	class WithArtifacts {

		static Path createArtifact(Path baseDir, String name, String featureJson, String repoGroupPath, String jarName)
				throws IOException {
			Path artifactDir = baseDir.resolve(name);
			Path featuresDir = artifactDir.resolve("features");
			Files.createDirectories(featuresDir);
			Files.writeString(featuresDir.resolve(jarName.replace(".jar", "") + "-feature.json"), featureJson);

			Path repoDir = artifactDir.resolve("repo").resolve(repoGroupPath);
			Files.createDirectories(repoDir);
			Files.writeString(repoDir.resolve(jarName), "dummy-jar-content");
			return artifactDir;
		}

		static String featureJson(String id) {
			return """
					{
					  "id": "%s",
					  "name": "Test Feature",
					  "complete": true,
					  "bundles": []
					}
					""".formatted(id);
		}

		@Test
		void shouldMergeMultipleArtifacts(@TempDir Path tempDir) throws IOException {
			Path artifact1 = createArtifact(tempDir, "jetty", featureJson("org.example:jetty-feature:1.0.0"),
					"org/eclipse/jetty/jetty-server/12.0.0", "jetty-server-12.0.0.jar");
			Path artifact2 = createArtifact(tempDir, "jaxrs", featureJson("org.example:jaxrs-feature:1.0.0"),
					"org/example/jaxrs/jaxrs-impl/1.0.0", "jaxrs-impl-1.0.0.jar");

			try (GenericContainer<?> container = createContainer()
					.withFileSystemBind(artifact1.toString(), "/app/initial-content-load/jetty", BindMode.READ_ONLY)
					.withFileSystemBind(artifact2.toString(), "/app/initial-content-load/jaxrs", BindMode.READ_ONLY)) {
				container.start();

				String logs = container.getLogs();
				assertThat(logs).contains("Merged feature: jetty/");
				assertThat(logs).contains("Merged feature: jaxrs/");
				assertThat(logs).contains("Merged repo:    jetty/repo");
				assertThat(logs).contains("Merged repo:    jaxrs/repo");
				assertThat(logs).contains("2 source(s)");
			}
		}

		@Test
		void shouldHandleDuplicateFeatureNames(@TempDir Path tempDir) throws IOException {
			// Both artifacts have a feature file with the same base name
			Path artifact1 = tempDir.resolve("artifact1");
			Files.createDirectories(artifact1.resolve("features"));
			Files.writeString(artifact1.resolve("features/feature.json"), featureJson("org.example:feature-a:1.0.0"));

			Path artifact2 = tempDir.resolve("artifact2");
			Files.createDirectories(artifact2.resolve("features"));
			Files.writeString(artifact2.resolve("features/feature.json"), featureJson("org.example:feature-b:1.0.0"));

			try (GenericContainer<?> container = createContainer()
					.withFileSystemBind(artifact1.toString(), "/app/initial-content-load/artifact1", BindMode.READ_ONLY)
					.withFileSystemBind(artifact2.toString(), "/app/initial-content-load/artifact2",
							BindMode.READ_ONLY)) {
				container.start();

				String logs = container.getLogs();
				// Both features merged with UUID suffix — two distinct "Merged feature:" lines
				assertThat(logs).contains("Merged feature: artifact1/feature.json -> feature-");
				assertThat(logs).contains("Merged feature: artifact2/feature.json -> feature-");
				assertThat(logs).contains("2 source(s)");
			}
		}

		@Test
		void shouldSkipOnEmptyContentDir() {
			try (GenericContainer<?> container = createContainer()) {
				container.start();

				String logs = container.getLogs();
				assertThat(logs).contains("0 source(s)");
			}
		}
	}
}
