import ballerina/openapi;
import ballerina/http;

type Response record {|
    *http:Ok;
    Test body;
|};

type Test record {|
    int id;
    string name;
|};
@openapi:ServiceInfo {
    contract:"single_record.yaml"
}

service / on new http:Listener(9090) {
    resource function get .() returns Response {
        return {
            body: {
                id: 1,
                name: "test"
            }
        };
    }
}
