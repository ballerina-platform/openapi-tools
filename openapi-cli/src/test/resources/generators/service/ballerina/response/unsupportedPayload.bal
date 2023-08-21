import ballerina/http;

listener http:Listener ep0 = new (80, config = {host: "petstore.openapi.io"});

service /v1 on ep0 {
    # Returns pet inventories by status
    #
    # + return - successful operation
    resource function get store/inventory() returns json {
    }
    # Description
    #
    # + return - successful operation
    resource function put store/inventory() returns json {
    }
    # Description
    #
    # + return - returns can be any of following types
    # OkJsonXml (successful operation)
    # BadRequestAnydata (bad request operation)
    # NotFoundAnydata (bad operation)
    # InternalServerErrorString (error operation)
    resource function post store/inventory() returns OkJsonXml|BadRequestAnydata|NotFoundAnydata|InternalServerErrorString {
    }
    # Description
    #
    # + return - successful operation
    resource function delete store/inventory() returns anydata {
    }
}
