import ballerina/openapi;
import ballerina/http;

@openapi:ServiceInfo {
    contract:"union_status_code.yaml"
}
service on new http:Listener(9090) {
    resource function get .() returns http:Ok | http:BadRequest | http:Unauthorized  | http:NotFound {
        return <http:Ok>{};
    }
}
