/*********************************************************************
* Copyright (c) 2026 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.osgi.technology.featurelauncher.container;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import com.sun.net.httpserver.HttpServer;

/**
 * Minimal HTTP server using JDK's built-in HttpServer to serve feature JSON for
 * HTTP installer container tests.
 */
class FeatureServer implements AutoCloseable {

	private final HttpServer server;
	private final AtomicReference<String> featureJson = new AtomicReference<>("[]");

	FeatureServer() throws IOException {
		this(0);
	}

	FeatureServer(int port) throws IOException {
		server = HttpServer.create(new InetSocketAddress(port), 0);
		server.createContext("/features", exchange -> {
			String response = featureJson.get();
			byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
			exchange.getResponseHeaders().set("Content-Type", "application/json");
			exchange.sendResponseHeaders(200, bytes.length);
			try (OutputStream os = exchange.getResponseBody()) {
				os.write(bytes);
			}
		});
		server.start();
	}

	int getPort() {
		return server.getAddress().getPort();
	}

	String getUrl() {
		return "http://host.testcontainers.internal:" + getPort() + "/features";
	}

	void setFeatureJson(String json) {
		featureJson.set(json);
	}

	@Override
	public void close() {
		server.stop(0);
	}
}
