// AUTO-GENERATED FILE. DO NOT MODIFY.
// This file is auto-generated by the Ballerina OpenAPI tool.

import ballerina/data.jsondata;
import ballerina/http;

# This is a generated connector from [Azure OpenAI Chat Completions API v2023-08-01-preview](https://learn.microsoft.com/en-us/azure/cognitive-services/openai/reference#chat-completions/) OpenAPI specification.
# The Azure Azure OpenAI Service REST API Chat Completions Endpoint will create completions for chat messages with the ChatGPT (preview) and GPT-4 (preview) models.
@display {label: "Azure OpenAI Chat", iconPath: "icon.png"}
public isolated client class Client {
    final http:Client clientEp;
    final readonly & ApiKeysConfig? apiKeyConfig;
    # Gets invoked to initialize the `connector`.
    # The connector initialization requires setting the API credentials.
    # Create an [Azure](https://azure.microsoft.com/en-us/features/azure-portal/) account, an [Azure OpenAI resource with a model deployed](https://learn.microsoft.com/en-us/azure/cognitive-services/openai/how-to/create-resource) and refer [this guide](https://learn.microsoft.com/en-us/azure/cognitive-services/openai/reference#authentication) to learn how to generate and use tokens
    #
    # + config - The configurations to be used when initializing the `connector` 
    # + serviceUrl - URL of the target service 
    # + return - An error if connector initialization failed 
    public isolated function init(ConnectionConfig config, string serviceUrl = "https://your-resource-name.openai.azure.com/openai") returns error? {
        http:ClientConfiguration httpClientConfig = {httpVersion: config.httpVersion, http1Settings: config.http1Settings, http2Settings: config.http2Settings, timeout: config.timeout, forwarded: config.forwarded, followRedirects: config.followRedirects, poolConfig: config.poolConfig, cache: config.cache, compression: config.compression, circuitBreaker: config.circuitBreaker, retryConfig: config.retryConfig, cookieConfig: config.cookieConfig, responseLimits: config.responseLimits, secureSocket: config.secureSocket, proxy: config.proxy, socketConfig: config.socketConfig, validation: config.validation, laxDataBinding: config.laxDataBinding};
        if config.auth is ApiKeysConfig {
            self.apiKeyConfig = (<ApiKeysConfig>config.auth).cloneReadOnly();
        } else {
            httpClientConfig.auth = <http:BearerTokenConfig>config.auth;
            self.apiKeyConfig = ();
        }
        self.clientEp = check new (serviceUrl, httpClientConfig);
    }

    # Creates a completion for the chat message
    #
    # + headers - Headers to be sent with the request 
    # + queries - Queries to be sent with the request 
    # + return - OK 
    @display {label: "Create Chat Completion"}
    resource isolated function post deployments/[string deployment\-id]/chat/completions(createChatCompletionRequest payload, map<string|string[]> headers = {}, *ChatCompletionsCreateQueries queries) returns createChatCompletionResponse|error {
        string resourcePath = string `/deployments/${getEncodedUri(deployment\-id)}/chat/completions`;
        map<anydata> headerValues = {...headers};
        if self.apiKeyConfig is ApiKeysConfig {
            headerValues["api-key"] = self.apiKeyConfig?.api\-key;
        }
        resourcePath = resourcePath + check getPathForQueryParam(queries);
        map<string|string[]> httpHeaders = http:getHeaderMap(headerValues);
        http:Request request = new;
        json jsonBody = jsondata:toJson(payload);
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request, httpHeaders);
    }

    # Using extensions to creates a completion for the chat messages.
    #
    # + headers - Headers to be sent with the request
    # + queries - Queries to be sent with the request
    # + return - OK
    @display {label: "Create Extensions Chat Completion"}
    resource isolated function post deployments/[string deployment\-id]/extensions/chat/completions(extensionsChatCompletionsRequest payload, map<string|string[]> headers = {}, *ExtensionsChatCompletionsCreateQueries queries) returns extensionsChatCompletionsResponse|error {
        string resourcePath = string `/deployments/${getEncodedUri(deployment\-id)}/extensions/chat/completions`;
        map<anydata> headerValues = {...headers};
        if self.apiKeyConfig is ApiKeysConfig {
            headerValues["api-key"] = self.apiKeyConfig?.api\-key;
        }
        resourcePath = resourcePath + check getPathForQueryParam(queries);
        map<string|string[]> httpHeaders = http:getHeaderMap(headerValues);
        http:Request request = new;
        json jsonBody = jsondata:toJson(payload);
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request, httpHeaders);
    }
}
