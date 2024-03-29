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

import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.types.ClientType;
import io.ballerina.runtime.api.types.MethodType;
import io.ballerina.runtime.api.types.RemoteMethodType;
import io.ballerina.runtime.api.types.ResourceMethodType;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;

import java.util.Arrays;
import java.util.Objects;

public class ClientUtil {

    static final String CLIENT_ERROR = "ClientError";

    public static BError createHttpError(String message, BError cause) {
        return ErrorCreator.createError(ModuleUtils.getPackage(), CLIENT_ERROR, StringUtils.fromString(message),
                cause, null);
    }

    public static String getResourceImplFunctionName(String functionName, BObject client) throws RuntimeException {
        ResourceMethodType resourceMethodNotFound = Arrays.stream(((ClientType) client.getOriginalType()).
                getResourceMethods()).filter(resourceMethod -> resourceMethod.getName().equals(functionName)).
                findFirst().orElseThrow(() -> new RuntimeException("Resource method not found"));
        return getImplFunctionName(resourceMethodNotFound);
    }

    public static String getRemoteImplFunctionName(String functionName, BObject client) throws RuntimeException {
        RemoteMethodType remoteMethodNotFound = Arrays.stream(((ClientType) client.getOriginalType()).
                        getRemoteMethods()).filter(remoteMethod -> remoteMethod.getName().equals(functionName)).
                findFirst().orElseThrow(() -> new RuntimeException("Remote method not found"));
        return getImplFunctionName(remoteMethodNotFound);
    }

    private static String getImplFunctionName(MethodType clientMethod) {
        BString methodImplKey = Arrays.stream(clientMethod.getAnnotations().getKeys()).filter(
                key -> key.getValue().contains("MethodImpl")).findFirst().
                orElseThrow(() -> new RuntimeException("Method implementation annotation not found"));
        BMap methodImplAnnotation = (BMap) clientMethod.getAnnotation(methodImplKey);
        BString implFunctionName = methodImplAnnotation.getStringValue(StringUtils.fromString("name"));
        if (Objects.isNull(implFunctionName)) {
            throw new RuntimeException("Method implementation function name not found");
        }
        return implFunctionName.getValue();
    }
}
