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
    # + return - An paged array of pets
    #
    # # Deprecated
    # This methods has deprecated since the Pet has deprecated
    @deprecated
    remote isolated function listPets(int? 'limit = ()) returns Pets|error {
        string resourcePath = string `/pets`;
        map<anydata> queryParam = {"limit": 'limit};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        Pets response = check self.clientEp-> get(resourcePath);
        return response;
    }
    # Create a pet
    #
    # + return - Null response
    remote isolated function createPet() returns http:Response|error {
        string resourcePath = string `/pets`;
        http:Request request = new;
        //TODO: Update the request as needed;
        http:Response response = check self.clientEp-> post(resourcePath, request);
        return response;
    }
    # Info for a specific pet
    #
    # + petId - The id of the pet to retrieve
    # + 'limit - The id of the pet to retrieve
    # # Deprecated parameters
    # + 'limit - Limit is deprecated from v2
    # + return - Expected response to a valid request
    #
    # # Deprecated
    @deprecated
    remote isolated function showPetById(string petId, @deprecated string 'limit) returns Pets|error {
        string resourcePath = string `/pets/${getEncodedUri(petId)}`;
        map<anydata> queryParam = {"limit": 'limit};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        Pets response = check self.clientEp-> get(resourcePath);
        return response;
    }
}

