import ballerina/http;

type Offeset record {
    string 'type;
    int id;
};

service /'limit on new http:Listener(9090) {
    # Query parameter
    #
    # + 'limit - Parameter Description
    resource function get steps/'from/date(string 'limit) returns string|error {
        return "Hello";
    }

    resource function get steps/[int 'join](@http:Header string 'limit) returns string|error {
        return "Hello";
    }

    resource function post steps(@http:Payload Offeset payload) returns string|error {
        return "Hello";
    }
}