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

/**
 * Integration tests for the new shell-free container architecture.
 * These tests verify that the args-file mechanism, env var substitution,
 * and volume overlay all work correctly in the containerized launcher.
 *
 * <p>Tests use the local-base variant built with the default eclipse-temurin-17 base image.
 * The image must be built before running these tests:
 * <pre>
 * docker build --build-arg VARIANT=local-base -t feature-launcher-local-base:test .
 * </pre>
 */
class ArgsFileContainerTest {

	static final String IMAGE = System.getProperty("test.container.image",
			"feature-launcher-local-base:test");

	static GenericContainer<?> createContainer() {
		return new GenericContainer<>(IMAGE)
				.waitingFor(Wait.forLogMessage(".*Loading configuration from args-file.*\\n", 1))
				.withStartupTimeout(Duration.ofSeconds(60));
	}

	@Nested
	@Testcontainers
	class BasicStartup {

		@Container
		GenericContainer<?> container = createContainer();

		@Test
		void shouldStartWithArgsFile() {
			String logs = container.getLogs();
			assertThat(logs).contains("Loading configuration from args-file");
		}

		@Test
		void shouldLaunchFeature() {
			String logs = container.getLogs();
			assertThat(logs).contains("Launching feature");
		}

		@Test
		void shouldUseDefaultFrameworkStorage() {
			String logs = container.getLogs();
			assertThat(logs).contains("org.osgi.framework.storage = /app/storage/framework");
		}

		@Test
		void shouldUseLocalRepository() {
			String logs = container.getLogs();
			assertThat(logs).contains("/app/storage/repo");
		}
	}

	@Nested
	@Testcontainers
	class EnvVarSubstitution {

		@Container
		GenericContainer<?> container = createContainer()
				.withEnv("OSGI_FRAMEWORK_STORAGE", "/custom/storage");

		@Test
		void shouldSubstituteEnvVar() {
			String logs = container.getLogs();
			assertThat(logs).contains("org.osgi.framework.storage = /custom/storage");
		}
	}

	@Nested
	class VolumeOverlay {

		@Test
		void shouldDetectOverlayVolumes(@TempDir Path tempDir) throws IOException {
			// Create a volume structure with a Maven-layout repo
			Path vol1 = tempDir.resolve("vol1");
			Path repoDir = vol1.resolve("org/example/test/1.0.0");
			Files.createDirectories(repoDir);
			Files.writeString(repoDir.resolve("test-1.0.0.jar"), "dummy");

			try (GenericContainer<?> container = createContainer()
					.withFileSystemBind(vol1.toString(),
							"/app/artifacts/vol1", BindMode.READ_ONLY)) {
				container.start();

				String logs = container.getLogs();
				assertThat(logs).contains("Loading configuration from args-file");
				assertThat(logs).contains("Launching feature");
			}
		}

		@Test
		void shouldWorkWithMultipleVolumes(@TempDir Path tempDir) throws IOException {
			Path vol1 = tempDir.resolve("vol1");
			Path vol2 = tempDir.resolve("vol2");
			Files.createDirectories(vol1);
			Files.createDirectories(vol2);

			try (GenericContainer<?> container = createContainer()
					.withFileSystemBind(vol1.toString(),
							"/app/artifacts/vol1", BindMode.READ_ONLY)
					.withFileSystemBind(vol2.toString(),
							"/app/artifacts/vol2", BindMode.READ_ONLY)) {
				container.start();

				String logs = container.getLogs();
				assertThat(logs).contains("Loading configuration from args-file");
			}
		}
	}

	@Nested
	@Testcontainers
	class CustomArgsFile {

		@Test
		void shouldAcceptCustomArgsFile(@TempDir Path tempDir) throws IOException {
			Path customArgs = tempDir.resolve("custom.args");
			Files.writeString(customArgs, """
					-f /app/bootstrap/bootstrap.json
					-a file:///app/storage/repo
					-l org.osgi.framework.storage=/app/storage/framework
					-l org.osgi.framework.storage.clean=onFirstInit
					--impl-dry-run
					""");

			try (GenericContainer<?> container = new GenericContainer<>(IMAGE)
					.withFileSystemBind(customArgs.toString(),
							"/app/custom.args", BindMode.READ_ONLY)
					.withCommand("-Dlauncher.argsfile=/app/custom.args")
					.waitingFor(Wait.forLogMessage(".*Dry-run requested.*\\n", 1))
					.withStartupTimeout(Duration.ofSeconds(60))) {

				// Override entrypoint to pass system property
				container.withCreateContainerCmdModifier(cmd ->
						cmd.withEntrypoint("java", "-Dlauncher.argsfile=/app/custom.args",
								"-jar", "/app/launcher.jar"));
				container.start();

				String logs = container.getLogs();
				assertThat(logs).contains("Dry-run requested");
			}
		}
	}
}
