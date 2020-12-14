import ballerina/http;
import ballerina/openapi;

listener http:Listener ep0 = new(9090);

@openapi:ServiceInfo {
    contract: ""
}

service /  on ep0 {

    resource function get user(http:Caller caller, http:Request req) returns error? {

    }
    resource function put user(http:Caller caller, http:Request req) returns error? {

    }
    resource function post user(http:Caller caller, http:Request req) returns error? {

    }

}