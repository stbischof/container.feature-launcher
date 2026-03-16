# container.feature-launcher

Container images for the [OSGi Feature Launcher](https://github.com/eclipse-osgi-technology/feature-launcher).

## Available Images

| Image | Launcher | Description |
|---|---|---|
| [feature-launcher-local-base](local/base/README.md) | `all.lite` | Base image with local-only bundle resolution |
| [feature-launcher-remote-base](remote/base/README.md) | `all.maven` | Base image with local + remote Maven resolution |
| [feature-launcher-local-directory-installer](local/directory-installer/README.md) | `all.lite` | Scans a local directory for feature files |
| [feature-launcher-remote-http-installer](remote/http-installer/README.md) | `all.maven` | Fetches features from an HTTP endpoint |

## Image Hierarchy

```
eclipse-temurin:21-jre-alpine
├── feature-launcher-local-base
│   └── feature-launcher-local-directory-installer
└── feature-launcher-remote-base
    └── feature-launcher-remote-http-installer
```

## Pull

```bash
# Local base
docker pull ghcr.io/eclipse-osgi-technology/feature-launcher-local-base:snapshot

# Remote base
docker pull ghcr.io/eclipse-osgi-technology/feature-launcher-remote-base:snapshot

# Directory installer
docker pull ghcr.io/eclipse-osgi-technology/feature-launcher-local-directory-installer:snapshot

# HTTP installer
docker pull ghcr.io/eclipse-osgi-technology/feature-launcher-remote-http-installer:snapshot
```

## Local vs Remote

- **Local** images use the `all.lite` launcher — bundles are resolved only from a local file repository (`/app/storage/repo`). All required bundles must be pre-staged.
- **Remote** images use the `all.maven` launcher — bundles can be resolved from both local and remote Maven repositories (Maven Central, custom repos).

## Building Locally

The project uses Maven with the [fabric8 docker-maven-plugin](https://dmp.fabric8.io/) to build all 4 Docker images and run integration tests via [Testcontainers](https://www.testcontainers.org/).

```bash
# Set DOCKER_HOST for your container runtime (Podman example)
export DOCKER_HOST=unix:///run/user/$(id -u)/podman/podman.sock

# Build all images and run tests
mvn clean install

# Build without tests
mvn clean package -DskipTests
```

Launcher JARs are downloaded as Maven dependencies from the [Sonatype Central Snapshots](https://central.sonatype.com/repository/maven-snapshots/) repository.

## License

[Eclipse Public License 2.0](https://www.eclipse.org/legal/epl-2.0/)
