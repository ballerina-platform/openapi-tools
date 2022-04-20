import ballerina/openapi;
import ballerina/http;

type Response record {|
    *http:Ok;
    string body;
|};

@openapi:ServiceInfo {
    contract:"single_record_payload_type_mismatch.yaml"
}

service / on new http:Listener(9090) {
    resource function get .() returns Response {
        return {
            body: "Testing"
        };
    }
}
