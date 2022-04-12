import ballerina/openapi;
import ballerina/http;

type Pet record {
    int id;
    string name;
    string 'type;
};

@openapi:ServiceInfo {
    contract:"undocumented_field.yaml"
}
service on new http:Listener(9090) {
    resource function post pet(@http:Payload Pet payload) {
    }
}
