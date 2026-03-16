# OSGi Feature Launcher - HTTP Installer

Container image extending the [remote base](../base/README.md) image with the **HTTP installer** bundle. This image fetches feature definitions from an HTTP endpoint and installs them automatically at startup.

## Image

```
ghcr.io/eclipse-osgi-technology/feature-launcher-remote-http-installer:snapshot
```

## Quick Start

```bash
docker run -p 8080:8080 \
  -e OSGITECH_HTTP_FEATURES_URL=https://example.com/features.json \
  ghcr.io/eclipse-osgi-technology/feature-launcher-remote-http-installer:snapshot
```

## How It Works

1. Extends the `feature-launcher-remote-base` image (maven launcher, supports remote repositories)
2. The `installer.http.simple` bundle is included as a Maven dependency and pre-staged in the local repository
3. At startup, the HTTP installer fetches the feature list from the configured URL
4. Each feature is downloaded and installed via the OSGi Feature Runtime
5. Bundles are resolved from local and remote Maven repositories

## Volumes

| Path | Description |
|---|---|
| `/app/storage/repo` | Local Maven-layout artifact repository |
| `/app/storage/framework` | OSGi framework storage (managed automatically) |
| `/app/bootstrap/bootstrap.json` | Bootstrap feature (override to customize) |
| `/etc/osgitech/env.conf` | Environment override file |

## Environment Variables

### Base Configuration

| Variable | Default | Description |
|---|---|---|
| `OSGITECH_REPO_DIR` | `/app/storage/repo` | Local artifact repository |
| `OSGITECH_BOOTSTRAP_FEATURE` | `/app/bootstrap/bootstrap.json` | Bootstrap feature path |
| `JAVA_OPTS` | _(empty)_ | Additional JVM options |
| `JAVA_HEAP_MAX` | _(empty)_ | Maximum heap size (e.g. `512m`). When empty, JVM defaults apply |
| `OSGITECH_USE_DEFAULT_REPOS` | `true` | Enable default Maven repositories |
| `OSGITECH_EXTRA_REPOS` | _(empty)_ | Comma-separated list of additional Maven repository URLs |

### HTTP Installer Configuration

| Variable | Default | Description |
|---|---|---|
| `OSGITECH_HTTP_FEATURES_URL` | _(empty, **required**)_ | HTTP URL returning feature JSON. Supports `{serverId}` and `{frameworkId}` placeholders |
| `OSGITECH_HTTP_SCAN_MODE` | `ONCE` | Scan mode: `ONCE` (fetch at startup) or `WATCH` (periodic polling) |
| `OSGITECH_HTTP_SCAN_INTERVAL` | `300` | Polling interval in seconds (only used in `WATCH` mode) |
| `OSGITECH_HTTP_CONNECT_TIMEOUT` | `30` | HTTP connect timeout in seconds |
| `OSGITECH_HTTP_REQUEST_TIMEOUT` | `60` | HTTP request timeout in seconds |
| `OSGITECH_HTTP_SERVER_ID` | _(empty)_ | Custom server identifier, replaces `{serverId}` placeholder in the features URL |

## Build Args

| Arg | Default | Description |
|---|---|---|
| `BASE_IMAGE` | `feature-launcher-remote-base:1.0.0-SNAPSHOT` | Base image to extend |

## Example: Watch Mode with Custom Interval

Poll for feature updates every 5 minutes:

```bash
docker run -p 8080:8080 \
  -e OSGITECH_HTTP_FEATURES_URL=https://example.com/features.json \
  -e OSGITECH_HTTP_SCAN_MODE=WATCH \
  -e OSGITECH_HTTP_SCAN_INTERVAL=300 \
  ghcr.io/eclipse-osgi-technology/feature-launcher-remote-http-installer:snapshot
```

## License

[Eclipse Public License 2.0](https://www.eclipse.org/legal/epl-2.0/)
