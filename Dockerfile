#*******************************************************************************
# Copyright (c) 2026 Contributors to the Eclipse Foundation.
#
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
#   Contributors:
#   Stefan Bischof (bipolis.org) - initial
#*******************************************************************************
#
# Shared multi-stage Dockerfile for all container variants.
#
# Build args:
#   BASE_IMAGE  - Runtime base image (temurin, chiseled, distroless)
#   VARIANT     - Launcher variant (local-base, remote-base, local-directory-installer, remote-http-installer)
#   RUN_USER    - User to run as (1000 for temurin, nonroot for distroless/chiseled)
#
# Example:
#   docker build \
#     --build-arg BASE_IMAGE=eclipse-temurin:17-jre-noble \
#     --build-arg VARIANT=local-directory-installer \
#     -t feature-launcher .

ARG BASE_IMAGE=eclipse-temurin:17-jre-noble

# === Builder stage (has shell for mkdir, etc.) ===
FROM eclipse-temurin:17-jre-noble AS builder

ARG VARIANT=local-base

WORKDIR /app

# Copy launcher JAR
COPY target/dependency/launcher.jar /app/launcher.jar

# Copy args-file for this variant
COPY argsfiles/${VARIANT}.args /app/launcher.args

# Copy bootstrap feature for this variant
COPY bootstrap/${VARIANT}.json /app/bootstrap/bootstrap.json

# Copy extra bundles (installer JARs) if present
COPY target/dependency/extras/ /app/storage/repo/

# Create all required directories
RUN mkdir -p /app/storage/features \
             /app/storage/repo \
             /app/storage/framework \
             /app/artifacts

# === Runtime stage (can be distroless/chiseled — no shell needed) ===
FROM ${BASE_IMAGE}

# RUN_USER: 1000 for temurin, nonroot for distroless/chiseled
ARG RUN_USER=1000

# Copy entire /app from builder with correct ownership
COPY --from=builder --chown=${RUN_USER} /app /app

WORKDIR /app

# Pre-declare volumes so bind-mounts auto-work
VOLUME ["/app/storage/repo", "/app/artifacts"]

USER ${RUN_USER}

# No shell needed — Java reads /app/launcher.args automatically
ENTRYPOINT ["java", "-jar", "/app/launcher.jar"]
