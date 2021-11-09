/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.ballerina.openapi.extension;

import org.ballerinalang.langserver.commons.registration.BallerinaServerCapability;

/**
 * Server capabilities for the OpenAPI converter service.
 *
 * @since 2.0.0
 */
public class OpenAPIServerCapabilities extends BallerinaServerCapability {

    private boolean generateOpenAPI;

    public OpenAPIServerCapabilities() {
        super(OpenAPIServiceConstants.CAPABILITY_NAME);
    }

    public boolean isGenerateOpenAPI() {
        return generateOpenAPI;
    }

    public void setGenerateOpenAPI(boolean generateOpenAPI) {
        this.generateOpenAPI = generateOpenAPI;
    }
}
