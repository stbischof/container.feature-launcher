# OSGi Feature Launcher - Remote Base

Base container image for the [OSGi Feature Launcher](https://github.com/eclipse-osgi-technology/feature-launcher) using the **maven** launcher variant. This image can resolve bundles from remote Maven repositories at runtime — no local artifact repository is required.

This is a base image — it ships with an empty bootstrap feature and does nothing on its own. Provide your own bootstrap feature to use it.

## Image

```
ghcr.io/eclipse-osgi-technology/feature-launcher-remote-base:snapshot
```

## Quick Start

```bash
docker run -p 8080:8080 \
  -v ./my-bootstrap.json:/app/bootstrap/bootstrap.json:ro \
  ghcr.io/eclipse-osgi-technology/feature-launcher-remote-base:snapshot
```

## Usage

The default bootstrap feature is empty — you must provide your own. There are two ways:

### Option 1: Mount over the default path

Mount your feature JSON over `/app/bootstrap/bootstrap.json`:

```bash
docker run \
  -v ./my-bootstrap.json:/app/bootstrap/bootstrap.json:ro \
  ghcr.io/eclipse-osgi-technology/feature-launcher-remote-base:snapshot
```

### Option 2: Mount at a custom path

Mount your feature JSON anywhere and point `OSGITECH_BOOTSTRAP_FEATURE` to it:

```bash
docker run \
  -v ./my-bootstrap.json:/opt/my-feature.json:ro \
  -e OSGITECH_BOOTSTRAP_FEATURE=/opt/my-feature.json \
  ghcr.io/eclipse-osgi-technology/feature-launcher-remote-base:snapshot
```

### With custom Maven repositories

By default, Maven Central is used. Add extra repositories via `OSGITECH_EXTRA_REPOS`:

```bash
docker run \
  -v ./my-bootstrap.json:/app/bootstrap/bootstrap.json:ro \
  -e OSGITECH_EXTRA_REPOS=https://repo.example.com/maven,https://repo2.example.com/maven \
  ghcr.io/eclipse-osgi-technology/feature-launcher-remote-base:snapshot
```

### As a base image

You can also use this image as a base for your own Dockerfile:

```dockerfile
FROM ghcr.io/eclipse-osgi-technology/feature-launcher-remote-base:snapshot
COPY my-bootstrap.json /app/bootstrap/bootstrap.json
```

Since this image uses the **maven** launcher, bundles are resolved from remote repositories automatically — no local artifact repository needs to be pre-staged.

## How It Works

1. The `all.maven` launcher JAR is included as a Maven dependency and copied into the image at build time
2. At startup, the launcher loads the bootstrap feature from the path configured by `OSGITECH_BOOTSTRAP_FEATURE` (default: `/app/bootstrap/bootstrap.json`)
3. Bundles are resolved from remote Maven repositories (Maven Central by default)

## Volumes

| Path | Description |
|---|---|
| `/app/bootstrap/bootstrap.json` | Bootstrap feature (override to customize) |
| `/etc/osgitech/env.conf` | Environment override file |

## Environment Variables

| Variable | Default | Description |
|---|---|---|
| `OSGITECH_BOOTSTRAP_FEATURE` | `/app/bootstrap/bootstrap.json` | Bootstrap feature path |
| `OSGITECH_REPO_DIR` | `/app/storage/repo` | Local artifact repository (optional cache) |
| `OSGITECH_USE_DEFAULT_REPOS` | `true` | Enable default Maven repositories |
| `OSGITECH_EXTRA_REPOS` | _(empty)_ | Comma-separated list of additional Maven repository URLs |
| `JAVA_OPTS` | _(empty)_ | Additional JVM options |
| `JAVA_HEAP_MAX` | _(empty)_ | Maximum heap size (e.g. `512m`). When empty, JVM defaults apply |

## Build Args

| Arg | Default | Description |
|---|---|---|
| `APP_UID` | `1000` | Application user UID |
| `APP_GID` | `1000` | Application group GID |

## License

[Eclipse Public License 2.0](https://www.eclipse.org/legal/epl-2.0/)
