import ballerina/openapi;
import ballerina/http;

@openapi:ServiceInfo {
    contract: "oas_header.yaml"
}
service /v1 on new http:Listener(9090) {
    resource function get pets() returns http:Accepted {
        return <http:Accepted>{};
    }
}
