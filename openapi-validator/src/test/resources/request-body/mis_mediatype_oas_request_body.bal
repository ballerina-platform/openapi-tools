import ballerina/openapi;
import ballerina/http;

@openapi:ServiceInfo {
    contract:"mis_mediatype_oas_request_body.yaml"
}
service /v1 on new http:Listener(9090) {
    resource function post pets(@http:Payload json payload) returns http:Accepted {
           return <http:Accepted>{};
    }
}
