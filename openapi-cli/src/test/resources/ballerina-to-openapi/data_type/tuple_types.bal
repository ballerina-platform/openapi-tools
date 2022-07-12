import ballerina/http;

public type Pet record {
    int id;
    [int, string, decimal, float, User] address;
    ReturnTypes? tuples;
    [int, decimal]? unionTuple;
};

public type User readonly & record {|
    int id;
    int age;
|};

public type ReturnTypes readonly & [int, decimal];

service /payloadV on new http:Listener(9090) {
    resource function post .(@http:Payload Pet payload) {

    }
}
