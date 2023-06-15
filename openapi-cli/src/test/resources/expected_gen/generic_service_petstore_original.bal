import ballerina/http;

listener http:Listener ep0 = new (443, config = {host: "petstore3.swagger.io"});

service /api/v3 on ep0 {
    resource function put pet(http:Caller caller, http:Request request) returns error? {
    }
    resource function post pet(http:Caller caller, http:Request request) returns error? {
    }
    resource function get pet/findByStatus(http:Caller caller, http:Request request) returns error? {
    }
    resource function get pet/findByTags(http:Caller caller, http:Request request) returns error? {
    }
    resource function get pet/[int petId](http:Caller caller, http:Request request) returns error? {
    }
    resource function post pet/[int petId](http:Caller caller, http:Request request) returns error? {
    }
    resource function delete pet/[int petId](http:Caller caller, http:Request request) returns error? {
    }
    resource function post pet/[int petId]/uploadImage(http:Caller caller, http:Request request) returns error? {
    }
    resource function get store/inventory(http:Caller caller, http:Request request) returns error? {
    }
    resource function post store/'order(http:Caller caller, http:Request request) returns error? {
    }
    resource function get store/'order/[int orderId](http:Caller caller, http:Request request) returns error? {
    }
    resource function delete store/'order/[int orderId](http:Caller caller, http:Request request) returns error? {
    }
    resource function post user(http:Caller caller, http:Request request) returns error? {
    }
    resource function post user/createWithList(http:Caller caller, http:Request request) returns error? {
    }
    resource function get user/login(http:Caller caller, http:Request request) returns error? {
    }
    resource function get user/logout(http:Caller caller, http:Request request) returns error? {
    }
    resource function get user/[string username](http:Caller caller, http:Request request) returns error? {
    }
    resource function put user/[string username](http:Caller caller, http:Request request) returns error? {
    }
    resource function delete user/[string username](http:Caller caller, http:Request request) returns error? {
    }
}
