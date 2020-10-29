import ballerina/http;
import ballerina/openapi;

listener http:Listener ep0 = new(9090);

@openapi:ServiceInfo {
    contract: ""
}
@http:ServiceConfig {
    basePath: "/"
}

service   on ep0 {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/user"
    }
    resource function 'operation1resource1 (http:Caller caller, http:Request req) returns error? {

    }

    @http:ResourceConfig {
        methods:["PUT"],
        path:"/user"
    }
    resource function 'operation1resource2 (http:Caller caller, http:Request req) returns error? {

    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/user"
    }
    resource function 'operation2resource1 (http:Caller caller, http:Request req) returns error? {

    }

}