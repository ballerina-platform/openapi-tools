import ballerina/http;

public isolated client class Client {
    final http:Client clientEp;
    # Gets invoked to initialize the `connector`.
    #
    # + clientConfig - The configurations to be used when initializing the `connector`
    # + serviceUrl - URL of the target service
    # + return - An error if connector initialization failed
    public isolated function init(string serviceUrl, http:ClientConfiguration clientConfig =  {}) returns error? {
        http:Client httpEp = check new (serviceUrl, clientConfig);
        self.clientEp = httpEp;
        return;
    }
    # Delete neither header nor request body.
    #
    # + order_id - Order ID
    # + risk_id - Order Risk ID
    # + return - Status OK
    remote isolated function delete_order_risk(string order_id, string risk_id) returns http:Response|error {
        string resourcePath = string `/admin/api/2021-10/orders/${getEncodedUri(order_id)}/risks/${getEncodedUri(risk_id)}.json`;
        http:Response response = check self.clientEp-> delete(resourcePath);
        return response;
    }
    # Delete with request body.
    #
    # + return - Status OK
    remote isolated function order_risk(json payload) returns http:Response|error {
        string resourcePath = string `/request-body`;
        http:Request request = new;
        request.setPayload(payload, "application/json");
        http:Response response = check self.clientEp->delete(resourcePath, request);
        return response;
    }
    # Delete with header.
    #
    # + xRequestId - Tests header 01
    # + return - Status OK
    remote isolated function deleteHeader(string xRequestId) returns http:Response|error {
        string resourcePath = string `/header`;
        map<any> headerValues = {"X-Request-ID": xRequestId};
        map<string|string[]> httpHeaders = getMapForHeaders(headerValues);
        http:Response response = check self.clientEp->delete(resourcePath, headers = httpHeaders);
        return response;
    }
    # Delete with header and request body.
    #
    # + xRequestId - Tests header 01
    # + return - Status OK
    remote isolated function deleteHeaderRequestBody(string xRequestId, json payload) returns http:Response|error {
        string resourcePath = string `/header-with-request-body`;
        map<any> headerValues = {"X-Request-ID": xRequestId};
        map<string|string[]> httpHeaders = getMapForHeaders(headerValues);
        http:Request request = new;
        request.setPayload(payload, "application/json");
        http:Response response = check self.clientEp->delete(resourcePath, request, httpHeaders);
        return response;
    }
}
