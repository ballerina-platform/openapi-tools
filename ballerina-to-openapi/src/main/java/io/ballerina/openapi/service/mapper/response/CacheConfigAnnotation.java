/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.openapi.service.mapper.response;

import java.util.ArrayList;
import java.util.List;
/**
 * This class uses to store all the cache configuration details.
 *
 * @since 1.0.0
 */
public class CacheConfigAnnotation {
    private boolean mustRevalidate = true;
    private boolean noCache = false;
    private boolean noStore = false;
    private boolean noTransform = false;
    private boolean isPrivate = false;
    private boolean proxyRevalidate = false;
    private int maxAge = 3600;
    private int sMaxAge = -1;
    private List<String> noCacheFields = new ArrayList<>();
    private List<String>  privateFields = new ArrayList<>();
    private boolean setETag = true;
    private boolean setLastModified = true;

    public CacheConfigAnnotation() {
    }

    public boolean isSetETag() {
        return setETag;
    }

    public void setSetETag(boolean setETag) {
        this.setETag = setETag;
    }

    public boolean isSetLastModified() {
        return setLastModified;
    }

    public void setSetLastModified(boolean setLastModified) {
        this.setLastModified = setLastModified;
    }

    public boolean isMustRevalidate() {
        return mustRevalidate;
    }

    public void setMustRevalidate(boolean mustRevalidate) {
        this.mustRevalidate = mustRevalidate;
    }

    public boolean isNoCache() {
        return noCache;
    }

    public void setNoCache(boolean noCache) {
        this.noCache = noCache;
    }

    public boolean isNoStore() {
        return noStore;
    }

    public void setNoStore(boolean noStore) {
        this.noStore = noStore;
    }

    public boolean isNoTransform() {
        return noTransform;
    }

    public void setNoTransform(boolean noTransform) {
        this.noTransform = noTransform;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }

    public boolean isProxyRevalidate() {
        return proxyRevalidate;
    }

    public void setProxyRevalidate(boolean proxyRevalidate) {
        this.proxyRevalidate = proxyRevalidate;
    }

    public int getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }

    public int getsMaxAge() {
        return sMaxAge;
    }

    public void setsMaxAge(int sMaxAge) {
        this.sMaxAge = sMaxAge;
    }

    public List<String> getNoCacheFields() {
        return noCacheFields;
    }

    public void setNoCacheFields(List<String> noCacheFields) {
        this.noCacheFields = noCacheFields;
    }

    public List<String> getPrivateFields() {
        return privateFields;
    }

    public void setPrivateFields(List<String> privateFields) {
        this.privateFields = privateFields;
    }
}
