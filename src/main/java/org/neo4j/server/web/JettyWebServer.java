/**
 * Copyright (c) 2002-2010 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.neo4j.server.web;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.thread.QueuedThreadPool;

import com.sun.jersey.spi.container.servlet.ServletContainer;

public class JettyWebServer implements WebServer {

    private Server jetty;
    private int jettyPort = 80;
    private ServletHolder servletHolder = new ServletHolder(ServletContainer.class);
    private Context context;

    public void start() {
        jetty = new Server(jettyPort);

        context = new Context(jetty, "/");
        context.addServlet(servletHolder, "/*");

        try {
            jetty.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void shutdown() {
        try {
            jetty.stop();
            jetty.join();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setPort(int portNo) {
        jettyPort = portNo;
    }

    public int getPort() {
        return jettyPort;
    }

    public void addPackages(String packageNames) {
        if (packageNames == null) {
            return;
        }
        
        servletHolder.setInitParameter("com.sun.jersey.config.property.packages", packageNames);

    }

    public void setMaxThreads(int maxThreads) {
        jetty.setThreadPool(new QueuedThreadPool(maxThreads));
    }

    public URI getBaseUri() throws URISyntaxException {
        StringBuilder sb = new StringBuilder();

        sb.append("http");
        if (jettyPort == 443) {
            sb.append("s");
        }
        sb.append("://");
        try {
            sb.append(InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException e) {
            sb.append("localhost");
        }

        if (jettyPort != 80) {
            sb.append(":");
            sb.append(jettyPort);
        }

        sb.append(context.getContextPath());

        return new URI(sb.toString());
    }

    public URI getWelcomeUri() throws URISyntaxException {
        return new URI(getBaseUri().toString() + "welcome.html");
    }
}
