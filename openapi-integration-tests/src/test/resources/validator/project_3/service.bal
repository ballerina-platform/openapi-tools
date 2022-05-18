import ballerina/http;
import ballerina/openapi;

type User record {
     int userName;
     string firstName?;
     string lastName?;
};
listener http:Listener ep = new(9090);

@openapi:ServiceInfo {
    contract: "openapi.yaml",
    tags: [],
    operations: [],
    failOnErrors: false
}
service /v1 on ep {
     resource function post pets(@http:Payload User  payload) returns error? {
     }
 }
