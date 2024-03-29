/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.openapi.client;

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.Future;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.async.Callback;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BObject;

public class GeneratedClient {

    public static Object getResource(Environment env, BObject client, BArray pathParams, BArray params) {
        String functionName = env.getFunctionName();
        String methodName = ClientUtil.getResourceImplFunctionName(functionName, client);
        return invokeClientMethod(env, client, pathParams, params, methodName);
    }

    public static Object getResourceWithoutPath(Environment env, BObject client, BArray params) {
        String functionName = env.getFunctionName();
        String methodName = ClientUtil.getResourceImplFunctionName(functionName, client);
        return invokeClientMethod(env, client, params, methodName);
    }

    public static Object get(Environment env, BObject client, BArray params) {
        String functionName = env.getFunctionName();
        String methodName = ClientUtil.getRemoteImplFunctionName(functionName, client);
        return invokeClientMethod(env, client, params, methodName);
    }

    private static Object invokeClientMethod(Environment env, BObject client, BArray path, BArray params,
                                             String methodName) {
        int pathLength = (int) path.getLength();
        int paramLength = (int) params.getLength();

        Object[] paramFeed = new Object[(pathLength + paramLength) * 2];
        for (int i = 0; i < pathLength; i++) {
            paramFeed[i * 2] = path.get(i);
            paramFeed[i * 2 + 1] = true;
        }
        for (int i = 0; i < paramLength; i++) {
            paramFeed[(pathLength + i) * 2] = params.get(i);
            paramFeed[(pathLength + i) * 2 + 1] = true;
        }

        return invokeClientMethod(env, client, methodName, paramFeed);
    }

    private static Object invokeClientMethod(Environment env, BObject client, BArray params,
                                             String methodName) {
        int paramLength = (int) params.getLength();

        Object[] paramFeed = new Object[paramLength * 2];
        for (int i = 0; i < paramLength; i++) {
            paramFeed[i * 2] = params.get(i);
            paramFeed[i * 2 + 1] = true;
        }
        return invokeClientMethod(env, client, methodName, paramFeed);
    }

    private static Object invokeClientMethod(Environment env, BObject client, String methodName, Object[] paramFeed) {
        Future balFuture = env.markAsync();
        env.getRuntime().invokeMethodAsyncSequentially(client, methodName, null, null, new Callback() {
            @Override
            public void notifySuccess(Object result) {
                balFuture.complete(result);
            }

            @Override
            public void notifyFailure(BError bError) {
                BError invocationError = ClientUtil.createHttpError("client method invocation failed: " +
                                bError.getErrorMessage(), bError);
                balFuture.complete(invocationError);
            }
        }, null, PredefinedTypes.TYPE_NULL, paramFeed);
        return null;
    }
}
