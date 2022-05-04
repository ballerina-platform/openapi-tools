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
    #
    # + 'version - Version Id
    # + versionName - Version Name
    # + return - Ok
    remote isolated function operationId04(int 'version, string versionName) returns string|error {
        string resourcePath = string `/v1/${getEncodedUri('version)}/version-name/${getEncodedUri(versionName)}`;
        string response = check self.clientEp-> get(resourcePath);
        return response;
    }
}
