import ballerina/http;

@http:ServiceConfig { basePath: "/v1" }
type OASServiceType service object {
    *http:ServiceContract;
    resource function post pets() returns anydata;
};
