import ballerina/http;

listener http:Listener ep0 = new (80, config = {host: "petstore.openapi.io"});

service /v1 on ep0 {
    resource function post pets() returns Pet[] {
    }
    resource function post pets02() returns string[] {
    }
    resource function post pets03() returns json {
    }
    resource function post pets04() returns http:Response {
    }
}
