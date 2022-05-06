import ballerina/http;

# OAuth2 Client Credintials Grant Configs
public type OAuth2PasswordGrantConfig record {|
    *http:OAuth2PasswordGrantConfig;
    # Token URL
    string tokenUrl = "https://test.salesforce.com/services/oauth2/token";
|};

public isolated client class Client {
    final http:Client clientEp;
    # Gets invoked to initialize the `connector`.
    #
    # + clientConfig - The configurations to be used when initializing the `connector` 
    # + serviceUrl - URL of the target service 
    # + return - An error if connector initialization failed 
    public isolated function init(http:ClientConfiguration clientConfig, string serviceUrl = "https://petstore.swagger.io:443/v2") returns error? {
        http:Client httpEp = check new (serviceUrl, clientConfig);
        self.clientEp = httpEp;
        return;
    }
    # Info for a specific pet
    #
    # + petId - The id of the pet to retrieve
    # + headerX - Header X
    # + return - Expected response to a valid request
    remote isolated function getPetInfo(string petId, string headerX) returns Pet|error {
        string resourcePath = string `/pets/management`;
        map<any> headerValues = {"headerX": headerX};
        map<anydata> queryParam = {"petId": petId};
        if self.apiKeyConfig is ApiKeysConfig {
            headerValues["api-key"] = self.apiKeyConfig?.apiKey;
            queryParam["api-key-2"] = self.apiKeyConfig?.apiKey2;
        }
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        map<string|string[]> httpHeaders = getMapForHeaders(headerValues);
        Pet response = check self.clientEp->get(resourcePath, httpHeaders);
        return response;
    }
    # Vote for a pet
    #
    # + return - Expected response to a valid request
    remote isolated function votePet() returns Pet|error {
        string resourcePath = string `/pets/management`;
        map<any> headerValues = {};
        map<anydata> queryParam = {};
        if self.apiKeyConfig is ApiKeysConfig {
            headerValues["api-key"] = self.apiKeyConfig?.apiKey;
            queryParam["api-key-2"] = self.apiKeyConfig?.apiKey2;
        }
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        map<string|string[]> httpHeaders = getMapForHeaders(headerValues);
        http:Request request = new;
        //TODO: Update the request as needed;
        Pet response = check self.clientEp->post(resourcePath, request, headers = httpHeaders);
        return response;
    }
    # Delete a pet
    #
    # + petId - The id of the pet to delete
    # + return - Expected response to a valid request
    remote isolated function deletePetInfo(string petId) returns Pet|error {
        string resourcePath = string `/pets/management`;
        map<any> headerValues = {};
        map<anydata> queryParam = {"petId": petId};
        if self.apiKeyConfig is ApiKeysConfig {
            headerValues["api-key"] = self.apiKeyConfig?.apiKey;
            queryParam["api-key-2"] = self.apiKeyConfig?.apiKey2;
        }
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        map<string|string[]> httpHeaders = getMapForHeaders(headerValues);
        Pet response = check self.clientEp->delete(resourcePath, httpHeaders);
        return response;
    }
    # Delete a pet 2
    #
    # + petId - The id of the pet to delete
    # + return - Expected response to a valid request
    remote isolated function deletePetInfo2(string petId) returns Pet|error {
        string resourcePath = string `/pets/management2`;
        map<any> headerValues = {"petId": petId};
        map<anydata> queryParam = {};
        if self.apiKeyConfig is ApiKeysConfig {
            headerValues["api-key"] = self.apiKeyConfig?.apiKey;
            queryParam["api-key-2"] = self.apiKeyConfig?.apiKey2;
        }
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        map<string|string[]> httpHeaders = getMapForHeaders(headerValues);
        Pet response = check self.clientEp->delete(resourcePath, httpHeaders);
        return response;
    }
}
