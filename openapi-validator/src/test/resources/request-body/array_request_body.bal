import ballerina/openapi;
import ballerina/http;

type Pet record {
    int id;
    string name;
};

@openapi:ServiceInfo {
    contract:"array_request_body.yaml"
}

service /v1 on new http:Listener(9090) {
    resource function post pets(@http:Payload Pet[] payload) returns http:Accepted {
           return <http:Accepted>{};
    }
}
