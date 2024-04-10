import ballerina/http;

public type openapiClientConfig record {
    string serviceUrl;
    http:ClientConfiguration clientConfig;
};

public client class openapiClient {
    public final http:Client clientEp;
    public openapiClientConfig config;

    public function init(openapiClientConfig config) {
        http:Client httpEp = checkpanic new(config.serviceUrl, {auth: config.clientConfig.auth, cache:
            config.clientConfig.cache});
        self.clientEp = httpEp;
        self.config = config;
        return;
    }

    //1.service with query parameters limit , 'limit
    remote function listPets(int 'limit) returns Pets {
        http:Client listPetsEp = self.clientEp;
        //Check if qutedIdntifer there , if there then remove and take name
        returnpanic listPetsEp->get(string `/pets?limit=${'limit}`);

    }
    // post return with string
    remote function createPets() returns string {
        http:Client createPetsEp = self.clientEp;

        //post message mapped to response description.
        returnpanic createPetsEp->post("/pets","successful");
    }

    //client cant be with multiple value
    // remote function showPetById(string petId) returns Pet|Error {
    //     http:Client showPetByIdEp = self.clientEp;
        returnpanic showPetByIdEp->get(string `/pets/${getEncodedUri(petId)}`);
    //     return response;
    // }

    remote function showPetById(string petId) returns http:Response {
        http:Client showPetByIdEp = self.clientEp;
        returnpanic showPetByIdEp->get(string `/pets/${getEncodedUri(petId)}`);
        // if response is http:Response {
            // handle the given payload and return
        // }
    }

    //targetType map payload this depends on the user intention what is is going to do with this .
    // if he wants to access more details better option to use is http:Response or error.
    // if he just need to check status or payload msg it is okey to return boolean value by checking status code/
    // remote function deletePet(int petId) returns boolean {
        // http:Client deletePetEp = self.clientEp;
        //can't use http:Accepted in targetType.
        return deletePetEp->delete(string `/pets/${getEncodedUri(petId)}`);
        // return http:Accepted;
        // return true;
    // }

//request body
    remote function createPet(Pet createPetBody) returns http:Response {
        http:Client createPetEp = self.clientEp;
        http:Request request = new;
        json createPetJsonBody = checkpanic createPetBody.cloneWithType(json);
        request.setPayload(createPetJsonBody);

        // TODO: Update the request as needed
        returnpanic createPetEp->post("/pet", request);
    }

    remote function getPet() returns string {
        http:Client listPetsEp = self.clientEp;
        //Check if qutedIdntifer there , if there then remove and take name
        returnpanic listPetsEp->get("/pet");

    }
//payload can be string, json, xml, byte[], record, and record[]
//Error with record ERROR [openapi_client.bal:(87:28,87:42)] incompatible types: expected '(string|xml|json|byte[]|ballerina/mime:1.1.0-alpha6:Entity[]|stream<byte[],ballerina/io:0.6.0-alpha6:Error?>)'
    remote function createUser(User createUserBody) returns http:Response {
        http:Client createUserEp = self.clientEp;
        http:Request request = new;
        json createUserJsonBody = checkpanic createUserBody.cloneWithType(json);
        request.setPayload(createUserJsonBody);
        // TODO: Update the request as needed
        returnpanic createUserEp->post("/user", request);

    }

    // remote function createImage(blob createImageBody) returns http:Response | error {
    //     http:Client createImageEp = self.clientEp;
    //     http:Request request = new;


    //     // TODO: Update the request as needed
        return createImageEp->post("/image", request);

    //     if (response is http:Response) {
    //         return response;
    //     }
    //     return <error>response;
    // }

    // remote function createMultipart('mime:Entity[] createMultipartBody) returns http:Response | error {
    //     http:Client createMultipartEp = self.clientEp;
    //     http:Request request = new;


    //     // TODO: Update the request as needed
        return createMultipartEp->post("/imagemulti", request);

    //     if (response is http:Response) {
    //         return response;
    //     }
    //     return <error>response;
    // }

    //scenarion with error code 404
    // remote function resource1() returns record {| *http:NotFound; string body; |} {
    //     http:Client resource1Ep = self.clientEp
    //     // TODO: Update the request as needed
        returnpanic resource1Ep->get("/ping");
    //     return response;
    // }
    remote function resource2() returns Pet[] {
        http:Client resource1Ep = self.clientEp;
        http:Request request = new;

        // TODO: Update the request as needed
        returnpanic resource1Ep->get("/ping2");

    }
}

