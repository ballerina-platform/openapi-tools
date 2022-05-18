import ballerina/openapi;
import ballerina/http;

@openapi:ServiceInfo {
    contract:"union_status_code.yaml"
}
service on new http:Listener(9090) {
    resource function get .() returns string | http:BadRequest | http:Unauthorized  | error {
        return "<http:Ok>{}";
    }
}
