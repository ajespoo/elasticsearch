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

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpResponse;

import java.net.SocketAddress;

/**
 * A wrapper implementation of Netty's {@link org.jboss.netty.channel.MessageEvent}
 * that enables passing around the HttpResponse for the request.
 */
public class HttpMessageEvent implements MessageEvent {

    /** Creates an HttpMessageEvent wrapper object from the base {@link MessageEvent}
     *
     * @param baseEvent the base {@link MessageEvent}
     * @param response the {@link HttpResponse}
     */
    public static HttpMessageEvent newEvent(final MessageEvent baseEvent, final HttpResponse response) {
        return new HttpMessageEvent(baseEvent, response);
    }

    // The base event object that provides the MessageEvent functionality
    // that this class implements.
    private final MessageEvent baseEvent;

    // The HttpResponse for the event, if it was created by a handler through the
    // course of an event traversing the pipeline.
    private final HttpResponse response;

    /**
     * Creates an HttpMessageEvent wrapper object from the base {@link MessageEvent}
     *
     * @param baseEvent the base {@link MessageEvent}
     * @param response the {@link HttpResponse}
     */
    HttpMessageEvent(final MessageEvent baseEvent, final HttpResponse response) {
        if (baseEvent == null) {
            throw new IllegalArgumentException("MessageEvent cannot be null.");
        }
        if (response == null) {
            throw new IllegalArgumentException("HttpResponse cannot be null.");
        }
        this.baseEvent = baseEvent;
        this.response = response;
    }

    @Override
    public Object getMessage() {
        return baseEvent.getMessage();
    }

    @Override
    public SocketAddress getRemoteAddress() {
        return baseEvent.getRemoteAddress();
    }

    @Override
    public Channel getChannel() {
        return baseEvent.getChannel();
    }

    @Override
    public ChannelFuture getFuture() {
        return baseEvent.getFuture();
    }

    /**
     * Get the HttpResponse for this event (which is an HttpRequest).
     *
     * @return the {@link HttpResponse} for this event.
     */
    public HttpResponse getHttpResponse() {
        return this.response;
    }

}
