import ballerina/http;

type NoContent record {|
    *http:NoContent;
|};

service /payloadV on new http:Listener(9090) {
    resource function get testPath() returns NoContent|error {
        return error("test");
    }
}
