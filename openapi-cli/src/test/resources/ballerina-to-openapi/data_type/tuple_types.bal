import ballerina/http;

public type Pet record {
    int id;
    [int, string, decimal, float, User] address;
    TupleType[]? tuples;
    [int, decimal]? unionTuple;
};

public type User readonly & record {|
    int id;
    int age;
|};

public type TupleType readonly & [int, decimal];

service /payloadV on new http:Listener(9090) {
    resource function post path1(@http:Payload Pet payload) {

    }

    resource function post path2(@http:Payload TupleType payload) {

    }

    resource function post path3(@http:Payload TupleType[]? payload) {

    }

    resource function post path4(@http:Payload [int, decimal, string]? payload) {

    }

    resource function post path5(@http:Payload [int, decimal, string...] payload) {

    }
}
