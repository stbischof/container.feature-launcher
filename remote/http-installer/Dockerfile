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

ARG BASE_IMAGE=feature-launcher-remote-base:1.0.0-SNAPSHOT
FROM ${BASE_IMAGE}

USER root

# Copy installer.http.simple JAR into local repo (Maven layout)
COPY --chown=appuser:appgroup \
  target/dependency/installer.http.simple-*.jar \
  /app/storage/repo/org/eclipse/osgi-technology/featurelauncher/extras/installer.http.simple/installer.http.simple.jar

# Copy bootstrap feature, start script and env.conf (overrides base image)
COPY --chown=appuser:appgroup bootstrap/bootstrap.json /app/bootstrap/bootstrap.json
COPY --chown=appuser:appgroup start /app/
COPY --chown=appuser:appgroup env.conf /app/
RUN chmod +x /app/start

USER appuser
