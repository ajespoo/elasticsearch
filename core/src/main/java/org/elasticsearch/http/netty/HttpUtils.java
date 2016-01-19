/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.http.netty;

import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;

import java.util.Optional;

import static org.elasticsearch.common.Strings.enLowerCase;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Values.CLOSE;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Values.KEEP_ALIVE;
import static org.jboss.netty.handler.codec.http.HttpMethod.CONNECT;
import static org.jboss.netty.handler.codec.http.HttpMethod.DELETE;
import static org.jboss.netty.handler.codec.http.HttpMethod.GET;
import static org.jboss.netty.handler.codec.http.HttpMethod.HEAD;
import static org.jboss.netty.handler.codec.http.HttpMethod.OPTIONS;
import static org.jboss.netty.handler.codec.http.HttpMethod.PATCH;
import static org.jboss.netty.handler.codec.http.HttpMethod.POST;
import static org.jboss.netty.handler.codec.http.HttpMethod.PUT;
import static org.jboss.netty.handler.codec.http.HttpMethod.TRACE;

/**
 * Some utils for working with Netty's Http functionality.
 */
public class HttpUtils {

    /**
     * Determine if the request protocol version is HTTP 1.0
     *
     * @param request the {@link HttpRequest}
     * @return {@code true} if the protocol version is HTTP 1.0, otherwise {@code false}
     */
    public static boolean isHttp10(HttpRequest request) {
        return request.getProtocolVersion().equals(HttpVersion.HTTP_1_0);
    }

    /**
     * Determine if the request connection should be closed on completion.
     *
     * @param request the {@link HttpRequest}
     * @return {@code true} if the connection should be closed, otherwise {@code false}
     */
    public static boolean isCloseConnection(HttpRequest request) {
        final boolean http10 = isHttp10(request);
        return CLOSE.equalsIgnoreCase(request.headers().get(CONNECTION)) ||
            (http10 && !KEEP_ALIVE.equalsIgnoreCase(request.headers().get(CONNECTION)));
    }

    /**
     * Create a new {@link HttpResponse} to transmit the response for the given request.
     *
     * @param request the {@link HttpRequest} to create a response for
     * @return the {@link HttpResponse} object
     */
    public static HttpResponse newResponse(HttpRequest request) {
        final boolean http10 = isHttp10(request);
        final boolean close = isCloseConnection(request);
        // Build the response object.
        HttpResponseStatus status = HttpResponseStatus.OK; // default to initialize
        org.jboss.netty.handler.codec.http.HttpResponse resp;
        if (http10) {
            resp = new DefaultHttpResponse(HttpVersion.HTTP_1_0, status);
            if (!close) {
                resp.headers().add(CONNECTION, "Keep-Alive");
            }
        } else {
            resp = new DefaultHttpResponse(HttpVersion.HTTP_1_1, status);
        }
        return resp;
    }

    /**
     * Convert a string representation of an HTTP method to its {@link HttpMethod} equivalent.
     *
     * @param method the method name to convert
     * @return an {@link Optional} that contains the HttpMethod, or empty if no match was found.
     */
    public static Optional<HttpMethod> toHttpMethod(final String method) {
        final String m = enLowerCase(method);
        HttpMethod httpm = null;
        if (enLowerCase(CONNECT.getName()).equals(m)) {
            httpm = CONNECT;
        } else if (enLowerCase(DELETE.getName()).equals(m)) {
            httpm = DELETE;
        } else if (enLowerCase(GET.getName()).equals(m)) {
            httpm = GET;
        } else if (enLowerCase(HEAD.getName()).equals(m)) {
            httpm = HEAD;
        } else if (enLowerCase(OPTIONS.getName()).equals(m)) {
            httpm = OPTIONS;
        } else if (enLowerCase(PATCH.getName()).equals(m)) {
            httpm = PATCH;
        } else if (enLowerCase(POST.getName()).equals(m)) {
            httpm = POST;
        } else if (enLowerCase(PUT.getName()).equals(m)) {
            httpm = PUT;
        } else if (enLowerCase(TRACE.getName()).equals(m)) {
            httpm = TRACE;
        }
        return (httpm != null ? Optional.of(httpm) : Optional.empty());
    }

}
