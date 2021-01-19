import ballerina/http;

public type openapipetstoreClientConfig record {
    string serviceUrl;
    http:ClientConfiguration clientConfig;
};

public client class openapipetstoreClient {
    public http:Client clientEp;
    public openapipetstoreClientConfig config;

    public function __init(openapipetstoreClientConfig config) {
        http:Client httpEp = checkpanic new(config.serviceUrl, {auth: config.clientConfig.auth, cache:
        config.clientConfig.cache});
        self.clientEp = httpEp;
        self.config = config;
    }

    remote function listPets() returns http:Response | error {
        http:Client listPetsEp = self.clientEp;
        http:Request request = new;

        // TODO: Update the request as needed
        var response = listPetsEp->get("/pets", message = request);

        if (response is http:Response) {
            return response;
        }
        return <error>response;
    }

    remote function resource1() returns http:Response | error {
        http:Client resource1Ep = self.clientEp;
        http:Request request = new;

        // TODO: Update the request as needed
        var response = resource1Ep->post("/pets", request);

        if (response is http:Response) {
            return response;
        }
        return <error>response;
    }

    remote function showPetById(string petId) returns http:Response | error {
        http:Client showPetByIdEp = self.clientEp;
        http:Request request = new;

        // TODO: Update the request as needed
        var response = showPetByIdEp->get(string `/pets/${petId}`, message = request);

        if (response is http:Response) {
            return response;
        }
        return <error>response;
    }
    
};
