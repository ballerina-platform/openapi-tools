import ballerina/http;

public isolated client class Client {
    final http:Client clientEp;
    # Gets invoked to initialize the `connector`.
    #
    # + clientConfig - The configurations to be used when initializing the `connector`
    # + serviceUrl - URL of the target service
    # + return - An error if connector initialization failed
    public isolated function init(http:ClientConfiguration clientConfig =  {}, string serviceUrl = "https://petstore.swagger.io:443/v2") returns error? {
        http:Client httpEp = check new (serviceUrl, clientConfig);
        self.clientEp = httpEp;
        return;
    }
    # List all pets
    #
    # + 'limit - How many items to return at one time (max 100)
    # + payload - Pet
    remote isolated function listPets(json payload, int? 'limit = ()) returns json|error {
        string path = string `/pets`;
        map<anydata> queryParam = {"limit": 'limit};
        path = path + check getPathForQueryParam(queryParam);
        http:Request request = new;
        request.setPayload(payload, "application/json");
        json response = check self.clientEp->post(path, request);
        return response;
    }
}
