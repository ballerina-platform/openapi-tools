import ballerina/http;

listener http:Listener ep0 = new (80, config = {host: "petstore.openapi.io"});

service /v1 on ep0 {
    resource function post user() returns http:Ok {
    }
    resource function post user02() returns http:Created {
    }
    resource function post user03() returns CreatedString {
    }
    resource function post user04() returns string|OkString {
    }
}
