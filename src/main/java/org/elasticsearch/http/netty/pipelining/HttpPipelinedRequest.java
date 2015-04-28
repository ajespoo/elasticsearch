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
package org.elasticsearch.http.netty.pipelining;

import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 *
 */
public class HttpPipelinedRequest {

    private final FullHttpRequest request;
    private final int sequenceId;

    public HttpPipelinedRequest(FullHttpRequest request, int sequenceId) {
        this.request = request;
        this.sequenceId = sequenceId;
    }

    public FullHttpRequest getRequest() {
        return request;
    }

    public HttpPipelinedResponse createHttpResponse(DefaultFullHttpResponse response, ChannelPromise promise) {
        return new HttpPipelinedResponse(response, promise, sequenceId);
    }
}
