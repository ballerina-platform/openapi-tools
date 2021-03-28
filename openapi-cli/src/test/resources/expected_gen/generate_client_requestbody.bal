import ballerina/http;

public type openapipetstoreClientConfig record {
    string serviceUrl;
    http:ClientConfiguration clientConfig;
};

public client class openapipetstoreClient {
    public http:Client clientEp;
    public openapipetstoreClientConfig config;

    public function init(openapipetstoreClientConfig config) {
        http:Client httpEp = checkpanic new(config.serviceUrl, {auth: config.clientConfig.auth, cache:
            config.clientConfig.cache});
        self.clientEp = httpEp;
        self.config = config;
    }

    remote function resource1(User resource1Body) returns http:Response | error {
        http:Client resource1Ep = self.clientEp;
        http:Request request = new;
        json resource1JsonBody = check resource1Body.cloneWithType(json);
        request.setPayload(resource1JsonBody);

        // TODO: Update the request as needed
        var response = check resource1Ep->post("/requestBody", request);

        if (response is http:Response) {
            return response;
        }
        return <error>response;
    }
}
