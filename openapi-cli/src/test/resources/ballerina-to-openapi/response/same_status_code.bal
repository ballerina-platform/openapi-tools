import ballerina/http;

type Pet record  {
    int id;
    string name;
    string tag?;
    string 'type?;
};

service /payloadV on new http:Listener(9090) {
    resource function get testPath() returns boolean|int|string|Pet|error {
        return "test";
    }
}
