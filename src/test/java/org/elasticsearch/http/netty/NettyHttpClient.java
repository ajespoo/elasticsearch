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

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;

import java.io.Closeable;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static io.netty.handler.codec.http.HttpHeaders.Names.HOST;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Tiny helper
 */
public class NettyHttpClient implements Closeable {

    private static final ESLogger logger = ESLoggerFactory.getLogger(NettyHttpClient.class.getName());

    private static final Function<? super FullHttpResponse, String> FUNCTION_RESPONSE_TO_CONTENT = new Function<FullHttpResponse, String>() {
        @Override
        public String apply(FullHttpResponse response) {
            return response.content().toString(Charsets.UTF_8);
        }
    };

    private static final Function<? super FullHttpResponse, String> FUNCTION_RESPONSE_OPAQUE_ID = new Function<FullHttpResponse, String>() {
        @Override
        public String apply(FullHttpResponse response) {
            return response.headers().get("X-Opaque-Id");
        }
    };

    public static Collection<String> returnHttpResponseBodies(Collection<FullHttpResponse> responses) {
        return Collections2.transform(responses, FUNCTION_RESPONSE_TO_CONTENT);
    }

    public static Collection<String> returnOpaqueIds(Collection<FullHttpResponse> responses) {
        return Collections2.transform(responses, FUNCTION_RESPONSE_OPAQUE_ID);
    }

    private final Bootstrap clientBootstrap;

    public NettyHttpClient() {
        clientBootstrap = new Bootstrap()
                .channel(NioSocketChannel.class)
                .group(new NioEventLoopGroup());

    }

    public synchronized Collection<FullHttpResponse> sendRequests(SocketAddress remoteAddress, String... uris) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(uris.length);
        final Collection<FullHttpResponse> content = Collections.synchronizedList(new ArrayList<FullHttpResponse>(uris.length));

        clientBootstrap.handler(new CountDownLatchHandler(latch, content));

        ChannelFuture channelFuture = null;
        try {
            channelFuture = clientBootstrap.connect(remoteAddress);
            channelFuture.await(1000);

            for (int i = 0; i < uris.length; i++) {
                final HttpRequest httpRequest = new DefaultFullHttpRequest(HTTP_1_1, GET, uris[i]);
                httpRequest.headers().add(HOST, "localhost");
                httpRequest.headers().add("X-Opaque-ID", String.valueOf(i));
                logger.info("Requesting {} with id {}", uris[i], i);
                ChannelFuture writeAndFlushFuture = channelFuture.channel().writeAndFlush(httpRequest);
                writeAndFlushFuture.syncUninterruptibly();
            }
            latch.await(5, TimeUnit.SECONDS);

        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            if (channelFuture != null) {
                channelFuture.channel().close();
            }
        }

        return content;
    }

    @Override
    public void close() {
        clientBootstrap.group().shutdownGracefully().awaitUninterruptibly();
    }

    /**
     * helper factory which adds returned data to a list and uses a count down latch to decide when done
     */
    public static class CountDownLatchHandler extends ChannelInitializer<SocketChannel> {
        private final CountDownLatch latch;
        private final Collection<FullHttpResponse> content;

        public CountDownLatchHandler(CountDownLatch latch, Collection<FullHttpResponse> content) {
            this.latch = latch;
            this.content = content;
        }

        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            final int maxBytes = new ByteSizeValue(100, ByteSizeUnit.MB).bytesAsInt();
            ch.pipeline().addLast(new HttpRequestEncoder());
            ch.pipeline().addLast(new HttpResponseDecoder());
            ch.pipeline().addLast(new HttpObjectAggregator(maxBytes));
            ch.pipeline().addLast(new SimpleChannelInboundHandler<HttpObject>() {

                @Override
                protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
                    FullHttpResponse response = (FullHttpResponse) msg;
                    content.add(response.copy());
                    latch.countDown();
                }

                @Override
                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                    super.exceptionCaught(ctx, cause);
                    cause.printStackTrace();
                    latch.countDown();
                }
            });
        }
    }

}
