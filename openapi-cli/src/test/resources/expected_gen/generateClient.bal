import ballerina/http;

//=====================================
//============Client Config============
//=====================================
public type openapipetstoreClientConfig record {
    string serviceUrl;
    http:ClientConfiguration clientConfig;
};

//==============================
//============Client============
//==============================
public type openapipetstoreClient client object {
    public http:Client clientEp;
    public openapipetstoreClientConfig config;

    public function __init(openapipetstoreClientConfig config) {
        http:Client httpEp = new(config.serviceUrl, {auth: config.clientConfig.auth, cache: config.clientConfig.cache});
        self.clientEp = httpEp;
        self.config = config;
    }

    public remote function listPets() returns http:Response | error {
        http:Client listPetsEp = self.clientEp;
        http:Request request = new;

        // TODO: Update the request as needed
        return check listPetsEp->get("/pets", message = request);
    }
    
    public remote function resource1() returns http:Response | error {
        http:Client resource1Ep = self.clientEp;
        http:Request request = new;

        // TODO: Update the request as needed
        return check resource1Ep->post("/pets", request);
    }
    
    public remote function showPetById(string petId) returns http:Response | error {
        http:Client showPetByIdEp = self.clientEp;
        http:Request request = new;

        // TODO: Update the request as needed
        return check showPetByIdEp->get(string `/pets/${petId}`, message = request);
    }
    
};
