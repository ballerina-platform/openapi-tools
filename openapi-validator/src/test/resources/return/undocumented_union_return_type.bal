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
    string id;
    string name;
    string 'type;
|};
@openapi:ServiceInfo {
    contract:"union_return_type.yaml"
}

service / on new http:Listener(9090) {
    resource function get .() returns Pet | http:NotFound |http:NotAcceptable {
        return <http:NotFound> {};
    }
}
