# container.feature-launcher

Container images for the [OSGi Feature Launcher](https://github.com/eclipse-osgi-technology/feature-launcher).

## Image Matrix

**4 Launcher Variants x 4 Base Images = 16 Images**

### Launcher Variants

| Variant | Launcher | Description |
|---------|----------|-------------|
| `local-base` | `all.lite` | Local-only bundle resolution |
| `remote-base` | `all.maven` | Local + Maven Central resolution |
| `local-directory-installer` | `all.lite` | Scans directories for features, auto-discovers mounted artifact volumes |
| `remote-http-installer` | `all.maven` | Fetches features from an HTTP endpoint |

### Base Images

| Tag | Base Image | Platforms | Shell |
|-----|-----------|-----------|-------|
| `eclipse-temurin-17` | `eclipse-temurin:17-jre-noble` | amd64, arm64, armv7 | yes |
| `eclipse-temurin-25` | `eclipse-temurin:25-jre-noble` | amd64, arm64, riscv64 | yes |
| `ubuntu-chiseled-21` | `ubuntu/jre:21-24.04_stable` | amd64, arm64 | no |
| `grc-distroless-25` | `gcr.io/distroless/java25-debian13:nonroot` | amd64, arm64 | no |

### Image Tags

```
ghcr.io/eclipse-osgi-technology/feature-launcher-{variant}:{base-tag}-snapshot
```

Examples:
```bash
docker pull ghcr.io/eclipse-osgi-technology/feature-launcher-local-directory-installer:eclipse-temurin-17-snapshot
docker pull ghcr.io/eclipse-osgi-technology/feature-launcher-remote-base:grc-distroless-25-snapshot
```

## Architecture

All images are **shell-free** — configuration is provided via an args-file (`/app/launcher.args`) that is read directly by the Java launcher. No shell scripts are used at runtime.

### Container Layout

```
/app/
├── launcher.jar          # Feature Launcher executable JAR
├── launcher.args         # CLI arguments (one per line, supports ${ENV_VAR:-default})
├── bootstrap/
│   └── bootstrap.json    # Bootstrap feature definition
└── storage/
    ├── repo/             # Local M2 repository (VOLUME)
    ├── features/         # Feature JSON files (directory-installer)
    └── framework/        # OSGi framework storage
```

The `local-directory-installer` variant additionally provides:
```
/app/artifacts/           # Mount point for artifact volumes (VOLUME)
```

### Args-File Format

The args-file supports all [OSGi Feature Launcher spec](https://osgi.github.io/osgi/cmpn/service.feature.launcher.html) options (Table 160.1). Environment variables are substituted using `${VAR}` or `${VAR:-default}` syntax.

```bash
# Example args-file
-f /app/bootstrap/bootstrap.json
-a file:///app/storage/repo
-l org.osgi.framework.storage=${OSGI_FRAMEWORK_STORAGE:-/app/storage/framework}
-l org.osgi.framework.storage.clean=${OSGI_FRAMEWORK_STORAGE_CLEAN:-onFirstInit}

# Extension handlers
-e extensionName=com.example.MyHandler
```

## Local vs Remote

- **Local** variants use `all.lite` — bundles are resolved only from local file repositories. No network access needed. All required bundles must be pre-staged or mounted.
- **Remote** variants use `all.maven` — bundles can be resolved from both local repositories and Maven Central (`https://repo1.maven.org/maven2/`).

## Environment Variables

### All Variants

| Variable | Default | Description |
|----------|---------|-------------|
| `OSGI_FRAMEWORK_STORAGE` | `/app/storage/framework` | OSGi framework storage directory |
| `OSGI_FRAMEWORK_STORAGE_CLEAN` | `onFirstInit` | Storage clean policy |

### Directory Installer (`local-directory-installer`)

| Variable | Default | Description |
|----------|---------|-------------|
| `OSGITECH_FEATURES_DIR` | `/app/storage/features` | Directory to scan for feature JSON files |
| `OSGITECH_SCAN_MODE` | `ONCE` | `ONCE` (scan at startup) or `WATCH` (continuous polling) |
| `OSGITECH_SCAN_INTERVAL` | `300` | Polling interval in seconds (`WATCH` mode) |
| `OSGITECH_ARTIFACTS_DIR` | `/app/artifacts` | Base directory for mounted artifact volumes |

### HTTP Installer (`remote-http-installer`)

| Variable | Default | Description |
|----------|---------|-------------|
| `OSGITECH_HTTP_FEATURES_URL` | _(required)_ | URL to fetch feature definitions from |
| `OSGITECH_HTTP_SCAN_MODE` | `ONCE` | `ONCE` or `WATCH` |
| `OSGITECH_HTTP_SCAN_INTERVAL` | `300` | Polling interval in seconds |
| `OSGITECH_HTTP_CONNECT_TIMEOUT` | `30` | HTTP connection timeout (seconds) |
| `OSGITECH_HTTP_REQUEST_TIMEOUT` | `60` | HTTP request timeout (seconds) |

## Mounting Artifact Volumes (Directory Installer)

The `local-directory-installer` variant auto-discovers mounted artifact volumes under `/app/artifacts/`. Each child directory may contain:

- `repo/` — registered as a local repository for bundle resolution
- `features/` — scanned for feature JSON files

```bash
docker run \
  -v ./jetty-artifact:/app/artifacts/jetty:ro \
  -v ./jaxrs-artifact:/app/artifacts/jaxrs:ro \
  ghcr.io/eclipse-osgi-technology/feature-launcher-local-directory-installer:eclipse-temurin-17-snapshot
```

Expected artifact volume structure:
```
jetty-artifact/
├── repo/
│   └── org/eclipse/jetty/...    # Maven layout
└── features/
    └── jetty-feature.json
```

## Custom Configuration

Override the args-file to customize all launcher settings:

```bash
docker run \
  -v ./my-launcher.args:/app/launcher.args:ro \
  ghcr.io/eclipse-osgi-technology/feature-launcher-remote-base:eclipse-temurin-17-snapshot
```

### Adding extra Maven repositories (remote variants)

Create a custom args-file with additional `-a` lines:

```bash
# my-launcher.args
-f /app/bootstrap/bootstrap.json
-a file:///app/storage/repo
-a https://repo1.maven.org/maven2/,name=central
-a https://my-nexus.example.com/repository/releases/,name=corporate
-l org.osgi.framework.storage=/app/storage/framework
-l org.osgi.framework.storage.clean=onFirstInit
```

## Building Locally

Uses a single shared multi-stage Dockerfile with build args:

```bash
# Build a specific variant + base image
docker build \
  --build-arg BASE_IMAGE=eclipse-temurin:17-jre-noble \
  --build-arg VARIANT=local-directory-installer \
  -t feature-launcher-local-directory-installer:eclipse-temurin-17 .

# For distroless/chiseled (nonroot user)
docker build \
  --build-arg BASE_IMAGE=gcr.io/distroless/java25-debian13:nonroot \
  --build-arg VARIANT=local-base \
  --build-arg RUN_USER=nonroot \
  -t feature-launcher-local-base:grc-distroless-25 .
```

Prerequisites: download launcher JARs into `target/dependency/`:

```bash
mvn dependency:copy -Dartifact=org.eclipse.osgi-technology.featurelauncher.launch:all.lite:1.0.0-SNAPSHOT \
  -DoutputDirectory=target/dependency/ -Dmdep.stripVersion=true
mv target/dependency/all.lite.jar target/dependency/launcher.jar
mkdir -p target/dependency/extras
```

## License

[Eclipse Public License 2.0](https://www.eclipse.org/legal/epl-2.0/)
