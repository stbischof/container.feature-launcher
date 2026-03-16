# OSGi Feature Launcher - Local Base

Base container image for the [OSGi Feature Launcher](https://github.com/eclipse-osgi-technology/feature-launcher) using the **lite** launcher variant. This image resolves bundles only from a local file repository — it does not connect to remote Maven repositories at runtime.

This is a base image — it ships with an empty bootstrap feature and does nothing on its own. Provide your own bootstrap feature and a local artifact repository to use it.

## Image

```
ghcr.io/eclipse-osgi-technology/feature-launcher-local-base:snapshot
```

## Quick Start

```bash
docker run -p 8080:8080 \
  -v ./my-bootstrap.json:/app/bootstrap/bootstrap.json:ro \
  -v ./my-repo:/app/storage/repo \
  ghcr.io/eclipse-osgi-technology/feature-launcher-local-base:snapshot
```

## Usage

The default bootstrap feature is empty — you must provide your own. There are two ways:

### Option 1: Mount over the default path

Mount your feature JSON over `/app/bootstrap/bootstrap.json`:

```bash
docker run \
  -v ./my-bootstrap.json:/app/bootstrap/bootstrap.json:ro \
  -v ./my-repo:/app/storage/repo \
  ghcr.io/eclipse-osgi-technology/feature-launcher-local-base:snapshot
```

### Option 2: Mount at a custom path

Mount your feature JSON anywhere and point `OSGITECH_BOOTSTRAP_FEATURE` to it:

```bash
docker run \
  -v ./my-bootstrap.json:/opt/my-feature.json:ro \
  -e OSGITECH_BOOTSTRAP_FEATURE=/opt/my-feature.json \
  -v ./my-repo:/app/storage/repo \
  ghcr.io/eclipse-osgi-technology/feature-launcher-local-base:snapshot
```

### As a base image

You can also use this image as a base for your own Dockerfile:

```dockerfile
FROM ghcr.io/eclipse-osgi-technology/feature-launcher-local-base:snapshot
COPY my-bootstrap.json /app/bootstrap/bootstrap.json
COPY my-repo /app/storage/repo
```

Since this image uses the **lite** launcher, all required bundles must be pre-staged in the local repository (`/app/storage/repo`).

## How It Works

1. The `all.lite` launcher JAR is included as a Maven dependency and copied into the image at build time
2. At startup, the launcher loads the bootstrap feature from the path configured by `OSGITECH_BOOTSTRAP_FEATURE` (default: `/app/bootstrap/bootstrap.json`)
3. Bundles are resolved from the local file repository at `/app/storage/repo`

## Volumes

| Path | Description |
|---|---|
| `/app/storage/repo` | Local Maven-layout artifact repository |
| `/app/storage/framework` | OSGi framework storage (managed automatically) |
| `/app/bootstrap/bootstrap.json` | Bootstrap feature (override to customize) |
| `/etc/osgitech/env.conf` | Environment override file |

## Environment Variables

| Variable | Default | Description |
|---|---|---|
| `OSGITECH_BOOTSTRAP_FEATURE` | `/app/bootstrap/bootstrap.json` | Bootstrap feature path |
| `OSGITECH_REPO_DIR` | `/app/storage/repo` | Local artifact repository |
| `JAVA_OPTS` | _(empty)_ | Additional JVM options |
| `JAVA_HEAP_MAX` | _(empty)_ | Maximum heap size (e.g. `512m`). When empty, JVM defaults apply |

## Build Args

| Arg | Default | Description |
|---|---|---|
| `APP_UID` | `1000` | Application user UID |
| `APP_GID` | `1000` | Application group GID |

## License

[Eclipse Public License 2.0](https://www.eclipse.org/legal/epl-2.0/)
