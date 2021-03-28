import ballerina/http;
import ballerina/openapi;

listener http:Listener ep0 = new(80, config = {host: "petstore.openapi.io"});

listener http:Listener ep1 = new(443, config = {host: "petstore.swagger.io"});

@openapi:ServiceInfo {
    contract: "../../swagger/invalid/petstoreMethod.yaml",
    tags: [ ]
}

service petstore on ep0, ep1 {

    resource function get pets (http:Caller caller, http:Request req) returns error? {

    }
    //miss post method
    resource function get pets/[string petId] (http:Caller caller, http:Request req) returns error? {

    }

}
