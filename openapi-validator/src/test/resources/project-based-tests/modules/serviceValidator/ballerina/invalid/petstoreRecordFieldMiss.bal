import ballerina/http;
import ballerina/openapi;

type Pet record {
    int id;
    string name02;
    string 'type;
};

listener http:Listener ep1 = new (443, config = {host: "petstore.swagger.io"});

@openapi:ServiceInfo {
    contract: "..swagger/invalid/petstoreRecordFeildMiss.yaml",
    tags: []
}
service /putService on ep1 {
    resource function post pets(@http:Payload {} Pet payload) returns error? {

    }
}
