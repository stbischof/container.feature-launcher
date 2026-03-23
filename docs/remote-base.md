# OSGi Feature Launcher - Remote Base

OSGi Feature Launcher container with local and remote bundle resolution. Bundles are resolved from the local file repository and Maven Central.

Uses the `all.maven` launcher (full Maven resolver support).

## Available Tags

| Tag | Base Image | Platforms |
|-----|-----------|-----------|
| `eclipse-temurin-17-snapshot` | `eclipse-temurin:17-jre-noble` | amd64, arm64, armv7 |
| `eclipse-temurin-25-snapshot` | `eclipse-temurin:25-jre-noble` | amd64, arm64, riscv64 |
| `ubuntu-chiseled-21-snapshot` | `ubuntu/jre:21-24.04_stable` | amd64, arm64 |
| `grc-distroless-25-snapshot` | `gcr.io/distroless/java25-debian13:nonroot` | amd64, arm64 |

## Quick Start

```bash
docker pull ghcr.io/eclipse-osgi-technology/feature-launcher-remote-base:eclipse-temurin-17-snapshot

docker run ghcr.io/eclipse-osgi-technology/feature-launcher-remote-base:eclipse-temurin-17-snapshot
```

## Repositories

By default, this image resolves bundles from:
1. **Local repository** at `/app/storage/repo`
2. **Maven Central** at `https://repo1.maven.org/maven2/`

Additional repositories can be configured via a custom args-file.

## Volumes

| Path | Purpose |
|------|---------|
| `/app/storage/repo` | Local M2 repository for bundle resolution |

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `OSGI_FRAMEWORK_STORAGE` | `/app/storage/framework` | OSGi framework storage directory |
| `OSGI_FRAMEWORK_STORAGE_CLEAN` | `onFirstInit` | Framework storage clean policy |

## Custom Configuration

Mount a custom args-file to add extra repositories or change settings:

```bash
# custom-launcher.args
-f /app/bootstrap/bootstrap.json
-a file:///app/storage/repo
-a https://repo1.maven.org/maven2/,name=central
-a https://my-nexus.example.com/repository/releases/,name=corporate
-l org.osgi.framework.storage=/app/storage/framework
-l org.osgi.framework.storage.clean=onFirstInit
```

```bash
docker run \
  -v ./custom-launcher.args:/app/launcher.args:ro \
  ghcr.io/eclipse-osgi-technology/feature-launcher-remote-base:eclipse-temurin-17-snapshot
```

## License

[Eclipse Public License 2.0](https://www.eclipse.org/legal/epl-2.0/)
