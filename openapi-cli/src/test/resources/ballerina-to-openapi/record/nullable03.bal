import ballerina/http;

type Link record {|
    string? rel;
    Pet? pet?;
|};

type Pet record {|
    int id;
    string? name?;
|};

listener http:Listener ep0 = new(443, config = {host: "petstore.swagger.io"});

service /payloadV on ep0 {
    resource function post pet(@http:Payload Link payload) {
    }
}
