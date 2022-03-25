import ballerina/http;

type Pet record {
    string id;
};

service on new http:Listener(8980) {
    resource function get . (string id) {
    }
    resource function post . (@http:Payload Pet payload) {
    }
    resource function put . () {
    }
    resource function delete . () {
    }
}
