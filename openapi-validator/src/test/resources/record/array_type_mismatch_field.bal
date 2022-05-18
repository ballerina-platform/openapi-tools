import ballerina/openapi;
import ballerina/http;

type Pet record {
    int id;
    string name;
    string[] tags;
    string[] categories;
};

@openapi:ServiceInfo {
    contract:"array_type_mismatch_field.yaml"
}
service on new http:Listener(9090) {
    resource function post pet(@http:Payload Pet payload) {
    }
}
