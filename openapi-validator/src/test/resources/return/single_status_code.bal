import ballerina/openapi;
import ballerina/http;

@openapi:ServiceInfo {
    contract:"single_status_code.yaml"
}
service on new http:Listener(9090) {
    resource function get .() returns http:Ok {
        return <http:Ok>{};
    }
}
