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

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import org.elasticsearch.http.netty.pipelining.HttpPipelinedRequest;
import org.elasticsearch.rest.support.RestUtils;

import java.util.regex.Pattern;


/**
 *
 */
@ChannelHandler.Sharable
public class HttpRequestHandler extends SimpleChannelInboundHandler<Object> {

    private final NettyHttpServerTransport serverTransport;
    private final Pattern corsPattern;
    private final boolean httpPipeliningEnabled;
    private final boolean detailedErrorsEnabled;

    public HttpRequestHandler(NettyHttpServerTransport serverTransport, boolean detailedErrorsEnabled) {
        this.serverTransport = serverTransport;
        this.corsPattern = RestUtils.getCorsSettingRegex(serverTransport.settings());
        this.httpPipeliningEnabled = serverTransport.pipelining;
        this.detailedErrorsEnabled = detailedErrorsEnabled;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object message) throws Exception {
        FullHttpRequest request;
        HttpPipelinedRequest pipelinedRequest = null;
        if (this.httpPipeliningEnabled && message instanceof HttpPipelinedRequest) {
            pipelinedRequest = (HttpPipelinedRequest) message;
            request = pipelinedRequest.getRequest();
        } else {
            request = (FullHttpRequest) message;
        }

        // the netty HTTP handling always copy over the buffer to its own buffer, either in NioWorker internally
        // when reading, or using a cumulation buffer
        NettyHttpRequest httpRequest = new NettyHttpRequest(request, ctx.channel());
        if (pipelinedRequest != null) {
            serverTransport.dispatchRequest(httpRequest, new NettyHttpChannel(serverTransport, httpRequest, corsPattern, pipelinedRequest, detailedErrorsEnabled));
        } else {
            serverTransport.dispatchRequest(httpRequest, new NettyHttpChannel(serverTransport, httpRequest, corsPattern, detailedErrorsEnabled));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) throws Exception {
        serverTransport.exceptionCaught(ctx, e);
    }
}
