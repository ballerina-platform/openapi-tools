import ballerina/http;

# Provides API key configurations needed when communicating with a remote HTTP endpoint.
public type ApiKeysConfig record {|
    # Represents API Key `X-Shopify-Access-Token`
    string xShopifyAccessToken;
|};

public isolated client class Client {
    final http:Client clientEp;
    final readonly & ApiKeysConfig apiKeyConfig;
    # Gets invoked to initialize the `connector`.
    #
    # + apiKeyConfig - API keys for authorization
    # + clientConfig - The configurations to be used when initializing the `connector`
    # + serviceUrl - URL of the target service
    # + return - An error if connector initialization failed
    public isolated function init(ApiKeysConfig apiKeyConfig, string serviceUrl, http:ClientConfiguration clientConfig =  {}) returns error? {
        http:Client httpEp = check new (serviceUrl, clientConfig);
        self.clientEp = httpEp;
        self.apiKeyConfig = apiKeyConfig.cloneReadOnly();
        return;
    }
    # Deletes an order risk for an order.
    #
    # + orderId - Order ID
    # + riskId - Order Risk ID
    # + return - Status OK
    remote isolated function deleteOrderRisk(string orderId, string riskId) returns http:Response|error {
        string resourcePath = string `/admin/api/2021-10/orders/${getEncodedUri(orderId)}/risks/${getEncodedUri(riskId)}.json`;
        map<any> headerValues = {"X-Shopify-Access-Token": self.apiKeyConfig.xShopifyAccessToken};
        map<string|string[]> httpHeaders = getMapForHeaders(headerValues);
        http:Request request = new;
        //TODO: Update the request as needed;
        http:Response response = check self.clientEp->delete(resourcePath, request, headers = httpHeaders);
        return response;
    }
    # Deletes an order risk for an order.
    #
    # + return - Status OK
    remote isolated function orderRisk(json payload) returns http:Response|error {
        string resourcePath = string `/admin/api/2021-10`;
        map<any> headerValues = {"X-Shopify-Access-Token": self.apiKeyConfig.xShopifyAccessToken};
        map<string|string[]> httpHeaders = getMapForHeaders(headerValues);
        http:Request request = new;
        request.setPayload(payload, "application/json");
        http:Response response = check self.clientEp->delete(resourcePath, request, headers = httpHeaders);
        return response;
    }
}
