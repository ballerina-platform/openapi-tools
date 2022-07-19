import ballerina/http;

public isolated client class Client {
    final http:Client clientEp;
    # Gets invoked to initialize the `connector`.
    #
    # + clientConfig - The configurations to be used when initializing the `connector`
    # + serviceUrl - URL of the target service
    # + return - An error if connector initialization failed
    public isolated function init(http:ClientConfiguration clientConfig =  {}, string serviceUrl = "localhost:9090/payloadV") returns error? {
        http:Client httpEp = check new (serviceUrl, clientConfig);
        self.clientEp = httpEp;
        return;
    }
    # op1
    #
    # + return - Ok
    resource isolated function get .() returns string|error {
        string resourcePath = string `/`;
        string response = check self.clientEp->get(resourcePath);
        return response;
    }
    #
    # + return - Ok
    resource isolated function post .() returns string|error {
        string resourcePath = string `/`;
        http:Request request = new;
        //TODO: Update the request as needed;
        string response = check self.clientEp-> post(resourcePath, request);
        return response;
    }
    # op2
    #
    # + id - id value
    # + return - Ok
    resource isolated function get v1/[int id]() returns string|error {
        string resourcePath = string `/v1/${getEncodedUri(id)}`;
        string response = check self.clientEp->get(resourcePath);
        return response;
    }
    #
    # + return - Ok
    resource isolated function get v1/[int 'version]/v2/[string name]() returns string|error {
        string resourcePath = string `/v1/${getEncodedUri('version)}/v2/${getEncodedUri(name)}`;
        string response = check self.clientEp->get(resourcePath);
        return response;
    }
    #
    # + return - Ok
    resource isolated function get v1/[int 'version]/v2/[int 'limit]() returns string|error {
        string resourcePath = string `/v1/${getEncodedUri('version)}/v2/${getEncodedUri('limit)}`;
        string response = check self.clientEp->get(resourcePath);
        return response;
    }
    #
    # + return - Ok
    resource isolated function get v1/[int age]/v2/[string name]() returns string|error {
        string resourcePath = string `/v1/${getEncodedUri(age)}/v2/${getEncodedUri(name)}`;
        string response = check self.clientEp->get(resourcePath);
        return response;
    }
    # Retrieves a single customer.
    #
    # + customerId - Customer ID
    # + fields - Show only certain fields, specified by a comma-separated list of field names.
    # + return - Requested customer
    resource isolated function get admin/api/'2021\-10/customers/[string customerIdJson](string? fields = ()) returns http:Response|error {
        if !customerIdJson.endsWith(".json") {
            return error("bad URL");
        }
        string customerId = customerIdJson.substring(0, customerIdJson.length() - 4);
        string resourcePath = string `/admin/api/2021-10/customers/${getEncodedUri(customerId)}.json`;
        map<anydata> queryParam = {"fields": fields};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        http:Response response = check self.clientEp->get(resourcePath);
        return response;
    }
}
