import ballerina/openapi;
import ballerina/http;

type Response record {|
    *http:Accepted;
    string body;
|};

@openapi:ServiceInfo {
    contract:"single_status_code.yaml"
}

service / on new http:Listener(9090) {
    resource function get .() returns Response {
        return {
            body: "Testing"
        };
    }
}
