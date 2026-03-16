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

FROM eclipse-temurin:21-jre-alpine

# Set UID/GID - less Problems with Bind-Mounts (Podman rootless)
ARG APP_UID=1000
ARG APP_GID=1000

# Create non-root user for security
RUN addgroup -S -g ${APP_GID} appgroup \
 && adduser  -S -u ${APP_UID} -G appgroup appuser

# Set working directory
WORKDIR /app

# Copy launcher JAR (downloaded by maven-dependency-plugin)
COPY --chown=appuser:appgroup target/dependency/all.maven-*.jar /app/launcher.jar

# Copy application files
COPY --chown=appuser:appgroup start /app/
COPY --chown=appuser:appgroup env.conf /app/
COPY --chown=appuser:appgroup bootstrap/bootstrap.json /app/bootstrap/bootstrap.json

# Create directories for storage (features, repo, framework)
RUN mkdir -p /app/storage/features /app/storage/repo /app/storage/framework \
 && chown -R appuser:appgroup /app

# Make start script executable
RUN chmod +x /app/start

# Switch to non-root user
USER appuser

# Use shell script as entrypoint
ENTRYPOINT ["sh","-c","exec /app/start"]
