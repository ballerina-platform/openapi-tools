import ballerina/http;

type OASServiceType service object {
    *http:Service;
    resource function get pets(int:Signed32? 'limit) returns Pets|http:Response;
    resource function post pets() returns http:Created|http:Response;
    resource function get pets/[string petId]() returns Pets|http:Response;
};
