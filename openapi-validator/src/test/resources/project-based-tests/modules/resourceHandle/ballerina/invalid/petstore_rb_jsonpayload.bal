import ballerina/http;

type User record {
     int userName;
     string firstName?;
     string lastName?;
};
listener http:Listener ep0 = new(80, config = {host: "petstore.openapi.io"});
listener http:Listener ep1 = new(443, config = {host: "petstore.swagger.io"});
@openapi:ServiceInfo {
    contract: "../../swagger/invalid/petstore.yaml",
    tags: [],
    operations: [],
    failOnErrors: false
}
service /v1 on ep0, ep1 {
 #
 # + caller - Caller client object represents the endpoint
 # + req    - Req represents the message, which came over the network
 # + payload - Request body payload
 # + return - Error value if an error occurred or return `()` otherwise
     resource function post pets(http:Caller caller, http:Request req, @http:Payload {} User  payload)
      returns error? {}

 }

