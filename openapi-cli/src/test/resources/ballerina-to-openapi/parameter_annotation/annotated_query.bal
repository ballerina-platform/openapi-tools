import ballerina/http;

listener http:Listener ep0 = new (443, config = {host: "petstore3.swagger.io"});

type Student record {
    string Name;
};

service /payloadV on ep0 {

    resource function post student8(@http:Query map<json> student) returns json {
        return {Name: "john"};
    }
}
