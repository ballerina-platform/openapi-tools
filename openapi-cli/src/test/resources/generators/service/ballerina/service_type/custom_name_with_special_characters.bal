import ballerina/http;

@http:ServiceConfig { basePath: "/v1" }
type CustomServiceObjectTypeName1 service object {
    *http:ServiceContract;
    resource function post pets() returns InlineResponse400BadRequest;
};
