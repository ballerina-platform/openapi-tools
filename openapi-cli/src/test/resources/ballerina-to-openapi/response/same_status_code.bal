import ballerina/http;

type Pet record  {
    int id;
    string name;
    string tag?;
    string 'type?;
};

type Cat record  {
    int id;
    string name;
};

service /payloadV on new http:Listener(9090) {
    resource function get testPath() returns boolean|int|string|Pet|Cat|error {
        return "test";
    }
}
