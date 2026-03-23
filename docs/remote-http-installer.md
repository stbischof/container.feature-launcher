# OSGi Feature Launcher - Remote HTTP Installer

OSGi Feature Launcher container that fetches feature definitions from an HTTP endpoint and installs them at runtime. Bundles are resolved from the local file repository and Maven Central.

Uses the `all.maven` launcher with the `installer.http.simple` bundle.

## How It Works

1. The launcher starts with a bootstrap feature that includes the HTTP Installer bundle
2. The HTTP Installer fetches feature JSON from the configured `OSGITECH_HTTP_FEATURES_URL`
3. Fetched features are installed via the OSGi FeatureRuntime
4. Bundles referenced by features are resolved from local repo and Maven Central

## Available Tags

| Tag | Base Image | Platforms |
|-----|-----------|-----------|
| `eclipse-temurin-17-snapshot` | `eclipse-temurin:17-jre-noble` | amd64, arm64, armv7 |
| `eclipse-temurin-25-snapshot` | `eclipse-temurin:25-jre-noble` | amd64, arm64, riscv64 |
| `ubuntu-chiseled-21-snapshot` | `ubuntu/jre:21-24.04_stable` | amd64, arm64 |
| `grc-distroless-25-snapshot` | `gcr.io/distroless/java25-debian13:nonroot` | amd64, arm64 |

## Quick Start

```bash
docker pull ghcr.io/eclipse-osgi-technology/feature-launcher-remote-http-installer:eclipse-temurin-17-snapshot

docker run \
  -e OSGITECH_HTTP_FEATURES_URL=https://my-server.example.com/features \
  ghcr.io/eclipse-osgi-technology/feature-launcher-remote-http-installer:eclipse-temurin-17-snapshot
```

## Volumes

| Path | Purpose |
|------|---------|
| `/app/storage/repo` | Local M2 repository for bundle resolution |

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `OSGI_FRAMEWORK_STORAGE` | `/app/storage/framework` | OSGi framework storage directory |
| `OSGI_FRAMEWORK_STORAGE_CLEAN` | `onFirstInit` | Framework storage clean policy |
| `OSGITECH_HTTP_FEATURES_URL` | _(required)_ | URL to fetch feature definitions from |
| `OSGITECH_HTTP_SCAN_MODE` | `ONCE` | `ONCE` or `WATCH` |
| `OSGITECH_HTTP_SCAN_INTERVAL` | `300` | Polling interval in seconds (`WATCH` mode) |
| `OSGITECH_HTTP_CONNECT_TIMEOUT` | `30` | HTTP connection timeout (seconds) |
| `OSGITECH_HTTP_REQUEST_TIMEOUT` | `60` | HTTP request timeout (seconds) |

### Example: Watch mode polling a feature server

```bash
docker run \
  -e OSGITECH_HTTP_FEATURES_URL=https://config-server.example.com/features \
  -e OSGITECH_HTTP_SCAN_MODE=WATCH \
  -e OSGITECH_HTTP_SCAN_INTERVAL=60 \
  ghcr.io/eclipse-osgi-technology/feature-launcher-remote-http-installer:eclipse-temurin-17-snapshot
```

## Repositories

By default, this image resolves bundles from:
1. **Local repository** at `/app/storage/repo`
2. **Maven Central** at `https://repo1.maven.org/maven2/`

## Custom Configuration

Mount a custom args-file to add extra repositories or change settings:

```bash
docker run \
  -v ./my-launcher.args:/app/launcher.args:ro \
  ghcr.io/eclipse-osgi-technology/feature-launcher-remote-http-installer:eclipse-temurin-17-snapshot
```

## License

[Eclipse Public License 2.0](https://www.eclipse.org/legal/epl-2.0/)
