import ballerina/http;

public type User record {|
    string name;
    int age;
    never fullName?;
|};

service /payloadV on new http:Listener(9090) {

    resource function get info() returns User {
        return {name: "John Doe", age: 30};
    }
}
