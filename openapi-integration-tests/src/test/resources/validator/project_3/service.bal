import ballerina/http;
import ballerina/openapi;

type User record {
     int userName;
     string firstName?;
     string lastName?;
};
listener http:Listener ep1 = new(443, config = {host: "petstore.swagger.io"});
@openapi:ServiceInfo {
    contract: "openapi.yaml",
    tags: [],
    operations: [],
    failOnErrors: false
}
service /v1 on ep1 {
     resource function post pets(@http:Payload User  payload) returns error? {
     }
 }
