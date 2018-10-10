package com.mobiussoftware.iotbroker.mqtt.net;

/*
 * Copyright 2015 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

import io.netty.handler.codec.http.HttpHeaders;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public class EmptyHttpHeaders extends HttpHeaders {
    static final Iterator<Entry<CharSequence, CharSequence>> EMPTY_CHARS_ITERATOR =
            Collections.<Entry<CharSequence, CharSequence>>emptyList().iterator();

    public static final EmptyHttpHeaders INSTANCE = instance();

    /**
     * @see InstanceInitializer#EMPTY_HEADERS
     * @deprecated Use {@link EmptyHttpHeaders#INSTANCE}
     * <p>
     * This is needed to break a cyclic static initialization loop between {@link HttpHeaders} and {@link
     * EmptyHttpHeaders}.
     */
    @Deprecated
    static EmptyHttpHeaders instance() {
        return InstanceInitializer.EMPTY_HEADERS;
    }

    protected EmptyHttpHeaders() {
    }

    @Override
    public String get(String name) {
        return null;
    }

    @Override
    public List<String> getAll(String name) {
        return Collections.emptyList();
    }

    @Override
    public List<Entry<String, String>> entries() {
        return Collections.emptyList();
    }

    @Override
    public boolean contains(String name) {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public Set<String> names() {
        return Collections.emptySet();
    }

    @Override
    public HttpHeaders add(String name, Object value) {
        throw new UnsupportedOperationException("read only");
    }

    @Override
    public HttpHeaders add(String name, Iterable<?> values) {
        throw new UnsupportedOperationException("read only");
    }
    
    @Override
    public HttpHeaders set(String name, Object value) {
        throw new UnsupportedOperationException("read only");
    }

    @Override
    public HttpHeaders set(String name, Iterable<?> values) {
        throw new UnsupportedOperationException("read only");
    }   

    @Override
    public HttpHeaders remove(String name) {
        throw new UnsupportedOperationException("read only");
    }

    @Override
    public HttpHeaders clear() {
        throw new UnsupportedOperationException("read only");
    }

    @Override
    public Iterator<Entry<String, String>> iterator() {
        return entries().iterator();
    }

    /**
     * This class is needed to break a cyclic static initialization loop between {@link HttpHeaders} and
     * {@link EmptyHttpHeaders}.
     */
    @Deprecated
    private static final class InstanceInitializer {
        /**
         * The instance is instantiated here to break the cyclic static initialization between {@link EmptyHttpHeaders}
         * and {@link HttpHeaders}. The issue is that if someone accesses {@link EmptyHttpHeaders#INSTANCE} before
         * {@link HttpHeaders#EMPTY_HEADERS} then {@link HttpHeaders#EMPTY_HEADERS} will be {@code null}.
         */
        @Deprecated
        private static final EmptyHttpHeaders EMPTY_HEADERS = new EmptyHttpHeaders();

        private InstanceInitializer() {
        }
    }
}