import ballerina/openapi;
import ballerina/http;

type Pet record {
    int id;
    Type 'type;
};

type Type record {
    int id;
};

@openapi:ServiceInfo {
    contract:"type_mismatch_record.yaml"
}
service on new http:Listener(9090) {
    resource function post pet(@http:Payload Pet payload) {
    }
}
