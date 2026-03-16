# OSGi Feature Launcher - Directory Installer

Container image extending the [local base](../base/README.md) image with the **directory installer** bundle. This image scans a local directory for feature JSON files and installs them automatically at startup.

## Image

```
ghcr.io/eclipse-osgi-technology/feature-launcher-local-directory-installer:snapshot
```

## Quick Start

```bash
docker run \
  -v jetty-content:/app/initial-content-load/jetty \
  -v jaxrs-content:/app/initial-content-load/jaxrs \
  ghcr.io/eclipse-osgi-technology/feature-launcher-local-directory-installer:snapshot
```

## How It Works

1. Extends the `feature-launcher-local-base` image (lite launcher, local-only resolution)
2. The `installer.directory` bundle is included as a Maven dependency and pre-staged in the local repository
3. On first start, initial content mounted under `/app/initial-content-load/` is merged into the features and repo directories
4. The directory installer scans the configured features directory for `*.json` feature files
5. Discovered features are installed via the OSGi Feature Runtime
6. Bundles are resolved from the local repository at `/app/storage/repo`

## Initial Content Loading

Initial content loading allows you to compose your OSGi application from pre-packaged OCI artifacts (data-only images). Each artifact bundles a feature JSON together with all the JARs it needs, in a self-contained directory structure. At container startup, these are merged into the shared features and repository directories.

Content is loaded **only on first start**. A marker file `.content-loaded` is written to the features directory after the first successful load. Subsequent restarts detect this marker and skip the loading step.

### Content Structure

Each content source must be a directory with the following layout:

```
my-content/
├── features/                              # One or more OSGi Feature JSON files
│   ├── my-feature.json
│   └── another-feature.json
└── repo/                                  # Maven-layout repository with bundle JARs
    └── org/
        └── eclipse/
            └── jetty/
                └── jetty-server/
                    └── 12.0.0/
                        └── jetty-server-12.0.0.jar
```

**`features/`** — Contains OSGi Feature JSON files (any name, must end in `.json`). Each file defines a feature with its bundle references. During loading, feature files are copied with a UUID suffix (e.g. `my-feature-550e8400-e29b-41d4-a716-446655440000.json`) to prevent name collisions when multiple sources provide features with the same filename.

**`repo/`** — Contains bundle JARs in standard Maven repository layout. The directory structure follows the Maven convention: `groupId-path/artifactId/version/artifactId-version.jar` (where the groupId is split by `.` into path segments, e.g. `org.eclipse.jetty` becomes `org/eclipse/jetty`). All bundles referenced in the feature JSONs must be present here.

When multiple sources provide a JAR at the same Maven coordinate path, the first source processed wins — duplicates are skipped.

### Merge Strategy

The start script uses a hybrid approach for merging repository content:

1. **Feature files** are always copied (they are small and need UUID renaming)
2. **Repository JARs** are first attempted as hard links (`cp -rln`), which is instant and uses zero additional disk space when source and target are on the same filesystem. If hard linking fails (e.g. cross-device mounts), the script falls back to a regular copy (`cp -rn`).

### First Start vs. Subsequent Starts

**First start:**
- All subdirectories under `/app/initial-content-load/` are processed
- Feature JSONs are copied (with UUID suffix) into `/app/storage/features/`
- Repository contents are merged into `/app/storage/repo/`
- A marker file `.content-loaded` is created
- All operations are logged

**Subsequent starts:**
- The marker file is detected — loading is skipped entirely
- Log shows: `Initial content already loaded. Skipping.`

To force re-loading (e.g. after updating an artifact), delete the marker file:

```bash
docker exec <container> rm /app/storage/features/.content-loaded
```

## Container Setup Patterns

### Pattern 1: OCI Artifacts as Volumes

Build self-contained OCI artifacts (data-only images) for each feature set and mount them as volumes. This is the primary use case.

```bash
# Each OCI artifact image contains features/ and repo/ directories
docker run \
  -v jetty-servlet-artifact:/app/initial-content-load/jetty \
  -v jaxrs-artifact:/app/initial-content-load/jaxrs \
  -v h2-database-artifact:/app/initial-content-load/h2 \
  ghcr.io/eclipse-osgi-technology/feature-launcher-local-directory-installer:snapshot
```

### Pattern 2: Docker Compose

```yaml
services:
  feature-launcher:
    image: ghcr.io/eclipse-osgi-technology/feature-launcher-local-directory-installer:snapshot
    volumes:
      # Initial content from OCI artifacts
      - jetty-content:/app/initial-content-load/jetty
      - jaxrs-content:/app/initial-content-load/jaxrs
      - h2-content:/app/initial-content-load/h2
      # Persistent storage (survives restarts)
      - features-data:/app/storage/features
      - repo-data:/app/storage/repo
      - framework-data:/app/storage/framework
```

### Pattern 3: Kubernetes with Init Containers

Use init containers to populate a shared volume from OCI artifact images:

```yaml
apiVersion: v1
kind: Pod
spec:
  initContainers:
    - name: load-jetty
      image: ghcr.io/my-org/jetty-feature-artifact:1.0.0
      command: ['cp', '-r', '/content/.', '/initial-content/jetty/']
      volumeMounts:
        - name: initial-content
          mountPath: /initial-content/jetty
    - name: load-jaxrs
      image: ghcr.io/my-org/jaxrs-feature-artifact:1.0.0
      command: ['cp', '-r', '/content/.', '/initial-content/jaxrs/']
      volumeMounts:
        - name: initial-content
          mountPath: /initial-content/jaxrs
  containers:
    - name: feature-launcher
      image: ghcr.io/eclipse-osgi-technology/feature-launcher-local-directory-installer:snapshot
      volumeMounts:
        - name: initial-content
          mountPath: /app/initial-content-load
        - name: features
          mountPath: /app/storage/features
        - name: repo
          mountPath: /app/storage/repo
  volumes:
    - name: initial-content
      emptyDir: {}
    - name: features
      persistentVolumeClaim:
        claimName: features-pvc
    - name: repo
      persistentVolumeClaim:
        claimName: repo-pvc
```

### Pattern 4: Local Directories (Development)

For local development, mount host directories directly:

```bash
docker run \
  -v ./artifacts/jetty:/app/initial-content-load/jetty:ro \
  -v ./artifacts/jaxrs:/app/initial-content-load/jaxrs:ro \
  ghcr.io/eclipse-osgi-technology/feature-launcher-local-directory-installer:snapshot
```

### Building an OCI Feature Artifact

A feature artifact is a data-only OCI image. Example Dockerfile:

```dockerfile
FROM scratch
COPY features/ /content/features/
COPY repo/ /content/repo/
```

Build and push:

```bash
docker build -t ghcr.io/my-org/jetty-feature-artifact:1.0.0 .
docker push ghcr.io/my-org/jetty-feature-artifact:1.0.0
```

## Volumes

| Path | Description |
|---|---|
| `/app/initial-content-load/<name>` | Mount initial content sources here (each as a subdirectory) |
| `/app/storage/features` | Feature JSON files for automatic installation |
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

### Initial Content Loading

| Variable | Default | Description |
|---|---|---|
| `OSGITECH_INITIAL_CONTENT_DIR` | `/app/initial-content-load` | Root directory scanned for initial content subdirectories |

### Directory Installer Configuration

| Variable | Default | Description |
|---|---|---|
| `OSGITECH_DIR_INSTALLER_FEATURES_DIR` | `/app/storage/features` | Directory to scan for feature JSON files |
| `OSGITECH_DIR_INSTALLER_REPO_DIR` | `/app/storage/repo` | Local artifact repository for bundle resolution |
| `OSGITECH_DIR_INSTALLER_SCAN_MODE` | `ONCE` | Scan mode: `ONCE` (scan at startup) or `WATCH` (continuous monitoring) |
| `OSGITECH_DIR_INSTALLER_SCAN_INTERVAL` | `300` | Polling interval in seconds (only used in `WATCH` mode) |

## Build Args

| Arg | Default | Description |
|---|---|---|
| `BASE_IMAGE` | `feature-launcher-local-base:1.0.0-SNAPSHOT` | Base image to extend |

## Example: Watch Mode

To continuously scan for new features:

```bash
docker run \
  -e OSGITECH_DIR_INSTALLER_SCAN_MODE=WATCH \
  -v ./my-features:/app/storage/features \
  -v ./my-repo:/app/storage/repo \
  ghcr.io/eclipse-osgi-technology/feature-launcher-local-directory-installer:snapshot
```

## License

[Eclipse Public License 2.0](https://www.eclipse.org/legal/epl-2.0/)
