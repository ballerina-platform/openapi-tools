import ballerina/http;

type Link record {|
    string rel;
    Dog|Cat pet;
|};

type Dog record {|
    int? id;
    string name?;
|};

type Cat record {|
    int id;
    string eat?;
|};

listener http:Listener ep0 = new(443, config = {host: "petstore.swagger.io"});

service /payloadV on ep0 {
    resource function post pet(@http:Payload Link payload) {
    }
}
