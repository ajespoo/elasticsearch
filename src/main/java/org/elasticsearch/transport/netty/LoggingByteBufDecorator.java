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

package org.elasticsearch.transport.netty;

import io.netty.buffer.AbstractByteBuf;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufProcessor;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.util.UUID;

/**
 *
 */
// TODO REMOVE ME AGAIN, JUST A TESTING TOOL TO IDENTIFY EARLY CLOSED BYTEBUFS
public class LoggingByteBufDecorator extends AbstractByteBuf {

    private final ByteBuf buffer;
    private final UUID uuid;
    private static final ESLogger logger = ESLoggerFactory.getLogger(LoggingByteBufDecorator.class.getName());

    public LoggingByteBufDecorator(ByteBuf buffer) {
        super(buffer.maxCapacity());
        this.buffer = buffer;
        this.uuid = UUID.randomUUID();

        setIndex(buffer.readerIndex(), buffer.writerIndex());
    }



    @Override
    public final int refCnt() {
        return unwrap().refCnt();
    }

    @Override
    public final ByteBuf retain() {
        unwrap().retain();
        logger.info("retain(): {}, refCnt after {}", uuid, refCnt());
        return this;
    }

    @Override
    public final ByteBuf retain(int increment) {
        unwrap().retain(increment);
        return this;
    }

    @Override
    public final boolean release() {
        boolean release = unwrap().release();
        logger.info("release(): {}, refCnt after {}", uuid, refCnt());

        /*
        if (refCnt() == 0) {
            try {
                throw new ElasticsearchException("");
            } catch (ElasticsearchException e) {
                Loggers.getLogger("### XOXO ###").error(" retained... {} ", e, buffer);
            }
        }
        */

        return release;
    }

    @Override
    public final boolean release(int decrement) {
        return unwrap().release(decrement);
    }

    @Override
    public ByteBuffer nioBuffer(int index, int length) {
        return unwrap().nioBuffer(index, length);
    }

    @Override
    public ByteBuf unwrap() {
        return buffer;
    }

    @Override
    public ByteBufAllocator alloc() {
        return buffer.alloc();
    }

    @Override
    public ByteOrder order() {
        return buffer.order();
    }

    @Override
    public boolean isDirect() {
        return buffer.isDirect();
    }

    @Override
    public int capacity() {
        return buffer.capacity();
    }

    @Override
    public ByteBuf capacity(int newCapacity) {
        buffer.capacity(newCapacity);
        return this;
    }

    @Override
    public boolean hasArray() {
        return buffer.hasArray();
    }

    @Override
    public byte[] array() {
        return buffer.array();
    }

    @Override
    public int arrayOffset() {
        return buffer.arrayOffset();
    }

    @Override
    public boolean hasMemoryAddress() {
        return buffer.hasMemoryAddress();
    }

    @Override
    public long memoryAddress() {
        return buffer.memoryAddress();
    }

    @Override
    public byte getByte(int index) {
        return _getByte(index);
    }

    @Override
    protected byte _getByte(int index) {
        return buffer.getByte(index);
    }

    @Override
    public short getShort(int index) {
        return _getShort(index);
    }

    @Override
    protected short _getShort(int index) {
        return buffer.getShort(index);
    }

    @Override
    public int getUnsignedMedium(int index) {
        return _getUnsignedMedium(index);
    }

    @Override
    protected int _getUnsignedMedium(int index) {
        return buffer.getUnsignedMedium(index);
    }

    @Override
    public int getInt(int index) {
        return _getInt(index);
    }

    @Override
    protected int _getInt(int index) {
        return buffer.getInt(index);
    }

    @Override
    public long getLong(int index) {
        return _getLong(index);
    }

    @Override
    protected long _getLong(int index) {
        return buffer.getLong(index);
    }

    @Override
    public ByteBuf copy(int index, int length) {
        return buffer.copy(index, length);
    }

    @Override
    public ByteBuf slice(int index, int length) {
        return buffer.slice(index, length);
    }

    @Override
    public ByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length) {
        buffer.getBytes(index, dst, dstIndex, length);
        return this;
    }

    @Override
    public ByteBuf getBytes(int index, byte[] dst, int dstIndex, int length) {
        buffer.getBytes(index, dst, dstIndex, length);
        return this;
    }

    @Override
    public ByteBuf getBytes(int index, ByteBuffer dst) {
        buffer.getBytes(index, dst);
        return this;
    }

    @Override
    public ByteBuf setByte(int index, int value) {
        _setByte(index, value);
        return this;
    }

    @Override
    protected void _setByte(int index, int value) {
        buffer.setByte(index, value);
    }

    @Override
    public ByteBuf setShort(int index, int value) {
        _setShort(index, value);
        return this;
    }

    @Override
    protected void _setShort(int index, int value) {
        buffer.setShort(index, value);
    }

    @Override
    public ByteBuf setMedium(int index, int value) {
        _setMedium(index, value);
        return this;
    }

    @Override
    protected void _setMedium(int index, int value) {
        buffer.setMedium(index, value);
    }

    @Override
    public ByteBuf setInt(int index, int value) {
        _setInt(index, value);
        return this;
    }

    @Override
    protected void _setInt(int index, int value) {
        buffer.setInt(index, value);
    }

    @Override
    public ByteBuf setLong(int index, long value) {
        _setLong(index, value);
        return this;
    }

    @Override
    protected void _setLong(int index, long value) {
        buffer.setLong(index, value);
    }

    @Override
    public ByteBuf setBytes(int index, byte[] src, int srcIndex, int length) {
        buffer.setBytes(index, src, srcIndex, length);
        return this;
    }

    @Override
    public ByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length) {
        buffer.setBytes(index, src, srcIndex, length);
        return this;
    }

    @Override
    public ByteBuf setBytes(int index, ByteBuffer src) {
        buffer.setBytes(index, src);
        return this;
    }

    @Override
    public ByteBuf getBytes(int index, OutputStream out, int length)
            throws IOException {
        buffer.getBytes(index, out, length);
        return this;
    }

    @Override
    public int getBytes(int index, GatheringByteChannel out, int length)
            throws IOException {
        return buffer.getBytes(index, out, length);
    }

    @Override
    public int setBytes(int index, InputStream in, int length)
            throws IOException {
        return buffer.setBytes(index, in, length);
    }

    @Override
    public int setBytes(int index, ScatteringByteChannel in, int length)
            throws IOException {
        return buffer.setBytes(index, in, length);
    }

    @Override
    public int nioBufferCount() {
        return buffer.nioBufferCount();
    }

    @Override
    public ByteBuffer[] nioBuffers(int index, int length) {
        return buffer.nioBuffers(index, length);
    }

    @Override
    public ByteBuffer internalNioBuffer(int index, int length) {
        return nioBuffer(index, length);
    }

    @Override
    public int forEachByte(int index, int length, ByteBufProcessor processor) {
        return buffer.forEachByte(index, length, processor);
    }

    @Override
    public int forEachByteDesc(int index, int length, ByteBufProcessor processor) {
        return buffer.forEachByteDesc(index, length, processor);
    }

}
