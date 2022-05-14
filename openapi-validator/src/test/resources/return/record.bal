import ballerina/openapi;
import ballerina/http;

type Pet record {
    int id;
    string[][] address?;
};

@openapi:ServiceInfo {
    contract:"record.yaml"
}
service on new http:Listener(9090) {
    resource function get .() returns Pet {
        return {
            id: 1
        };
    }
}
