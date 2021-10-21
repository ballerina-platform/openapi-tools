import ballerina/http;

http:Client clientEP = check new ("http://postman-echo.com");

service / on new http:Listener(9090) {

    // The passthrough resource allows all HTTP methods as the accessor is `default`.
    resource function 'default passthrough(http:Request req) returns error? {
    }
}
