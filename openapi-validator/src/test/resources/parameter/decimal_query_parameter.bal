import ballerina/openapi;
import ballerina/http;

@openapi:ServiceInfo {
    contract:"decimal_query_parameter.yaml"
}
service /v1 on new http:Listener(9090) {
    resource function get pets(float offset) returns http:Accepted {
        return <http:Accepted> {};
    }
}
