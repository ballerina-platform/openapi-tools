import ballerina/openapi;
import ballerina/http;

type Response record {|
    *http:Accepted;
    Test body;
|};

type Test record {|
    int id;
    string name;
|};

type Pet record {|
    int id;
    string name;
    string 'type;
|};
@openapi:ServiceInfo {
    contract:"union_return_type.yaml"
}

service / on new http:Listener(9090) {
    resource function get .() returns Response | Pet | http:NotFound {
        return {
            body: {
                id: 1,
                name: "test",
                'type: "dog"
            }
        };
    }
}
