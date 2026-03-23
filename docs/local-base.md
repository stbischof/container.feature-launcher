# OSGi Feature Launcher - Local Base

Minimal OSGi Feature Launcher container with local-only bundle resolution. Bundles are resolved exclusively from the local file repository — no network access required.

Uses the `all.lite` launcher (lightweight, no Maven dependencies).

## Available Tags

| Tag | Base Image | Platforms |
|-----|-----------|-----------|
| `eclipse-temurin-17-snapshot` | `eclipse-temurin:17-jre-noble` | amd64, arm64, armv7 |
| `eclipse-temurin-25-snapshot` | `eclipse-temurin:25-jre-noble` | amd64, arm64, riscv64 |
| `ubuntu-chiseled-21-snapshot` | `ubuntu/jre:21-24.04_stable` | amd64, arm64 |
| `grc-distroless-25-snapshot` | `gcr.io/distroless/java25-debian13:nonroot` | amd64, arm64 |

## Quick Start

```bash
docker pull ghcr.io/eclipse-osgi-technology/feature-launcher-local-base:eclipse-temurin-17-snapshot

docker run ghcr.io/eclipse-osgi-technology/feature-launcher-local-base:eclipse-temurin-17-snapshot
```

## Volumes

| Path | Purpose |
|------|---------|
| `/app/storage/repo` | Local M2 repository for bundle resolution |

### Mount bundles at runtime

```bash
docker run \
  -v ./my-repo:/app/storage/repo:ro \
  ghcr.io/eclipse-osgi-technology/feature-launcher-local-base:eclipse-temurin-17-snapshot
```

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `OSGI_FRAMEWORK_STORAGE` | `/app/storage/framework` | OSGi framework storage directory |
| `OSGI_FRAMEWORK_STORAGE_CLEAN` | `onFirstInit` | Framework storage clean policy |

## Custom Configuration

Mount a custom args-file to override all launcher settings:

```bash
docker run \
  -v ./my-launcher.args:/app/launcher.args:ro \
  ghcr.io/eclipse-osgi-technology/feature-launcher-local-base:eclipse-temurin-17-snapshot
```

## License

[Eclipse Public License 2.0](https://www.eclipse.org/legal/epl-2.0/)
