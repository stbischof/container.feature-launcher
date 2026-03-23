# OSGi Feature Launcher - Local Directory Installer

OSGi Feature Launcher container that automatically discovers mounted artifact volumes and installs their features at runtime. Bundles are resolved from the local file repository only.

Uses the `all.lite` launcher with the `installer.directory` bundle and the `hash.checker` extension.

## How It Works

1. The launcher starts with a bootstrap feature that includes the Directory Installer and Hash Checker bundles
2. The Directory Installer scans `/app/storage/features` for `*.json` feature files
3. If configured, it also discovers artifact volumes under `/app/artifacts/`:
   - Each child directory's `repo/` subdirectory is registered as a repository
   - Each child directory's `features/` subdirectory is scanned for feature files
4. Discovered features are installed via the OSGi FeatureRuntime
5. Bundle integrity can be verified via the Hash Checker extension

## Available Tags

| Tag | Base Image | Platforms |
|-----|-----------|-----------|
| `eclipse-temurin-17-snapshot` | `eclipse-temurin:17-jre-noble` | amd64, arm64, armv7 |
| `eclipse-temurin-25-snapshot` | `eclipse-temurin:25-jre-noble` | amd64, arm64, riscv64 |
| `ubuntu-chiseled-21-snapshot` | `ubuntu/jre:21-24.04_stable` | amd64, arm64 |
| `grc-distroless-25-snapshot` | `gcr.io/distroless/java25-debian13:nonroot` | amd64, arm64 |

## Quick Start

```bash
docker pull ghcr.io/eclipse-osgi-technology/feature-launcher-local-directory-installer:eclipse-temurin-17-snapshot

docker run \
  -v ./my-features:/app/storage/features:ro \
  -v ./my-repo:/app/storage/repo:ro \
  ghcr.io/eclipse-osgi-technology/feature-launcher-local-directory-installer:eclipse-temurin-17-snapshot
```

## Mounting Artifact Volumes

Mount artifact volumes under `/app/artifacts/`. Each child directory may contain `repo/` and `features/` subdirectories:

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

## Volumes

| Path | Purpose |
|------|---------|
| `/app/storage/repo` | Local M2 repository for bundle resolution |
| `/app/storage/features` | Directory scanned for feature JSON files |
| `/app/artifacts` | Mount point for artifact volumes (auto-discovered) |

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `OSGI_FRAMEWORK_STORAGE` | `/app/storage/framework` | OSGi framework storage directory |
| `OSGI_FRAMEWORK_STORAGE_CLEAN` | `onFirstInit` | Framework storage clean policy |
| `OSGITECH_FEATURES_DIR` | `/app/storage/features` | Directory to scan for feature JSON files |
| `OSGITECH_SCAN_MODE` | `ONCE` | `ONCE` (scan at startup) or `WATCH` (continuous polling) |
| `OSGITECH_SCAN_INTERVAL` | `300` | Polling interval in seconds (`WATCH` mode) |
| `OSGITECH_ARTIFACTS_DIR` | `/app/artifacts` | Base directory for mounted artifact volumes |

### Example: Watch mode with 10-second interval

```bash
docker run \
  -e OSGITECH_SCAN_MODE=WATCH \
  -e OSGITECH_SCAN_INTERVAL=10 \
  -v ./features:/app/storage/features \
  -v ./repo:/app/storage/repo \
  ghcr.io/eclipse-osgi-technology/feature-launcher-local-directory-installer:eclipse-temurin-17-snapshot
```

## Custom Configuration

Mount a custom args-file to override all launcher settings:

```bash
docker run \
  -v ./my-launcher.args:/app/launcher.args:ro \
  ghcr.io/eclipse-osgi-technology/feature-launcher-local-directory-installer:eclipse-temurin-17-snapshot
```

## License

[Eclipse Public License 2.0](https://www.eclipse.org/legal/epl-2.0/)
