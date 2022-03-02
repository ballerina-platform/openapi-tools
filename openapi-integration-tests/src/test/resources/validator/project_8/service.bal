import ballerina/openapi;
import ballerina/http;

type Pet record {
    string id;
};

@openapi:ServiceInfo{
    contract: "openapi.yaml"
}
service on new http:Listener(8980) {
    resource function get . (int id) {

    }
    resource function post . (@http:Payload Pet payload) {

    }
    resource function put . () {

    }
    resource function delete . () {

    }
}
