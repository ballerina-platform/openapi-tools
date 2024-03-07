import ballerina/http;

public type User record {
    string name?;
    int id?;
};

public type OkString record {|
    *http:Ok;
    string body;
|};

public type OkStringWithHeader record {|
    *http:Ok;
    string body;
    record{|
        string header1;
        string header2;
    |} headers;
|};

service /payloadV on new http:Listener(9090) {

    //POST method is with default value
    resource function post pet() returns string {
        return "post";
    }

    resource function post pet2() returns User {
        User u = {
            name: "lna",
            id: 94
        };
        return u;
    }

    //With cache header details
    resource function post cachingBackEnd() returns @http:Cache string {
        return "Hello, World!!";
    }

    //With payload annotation for overriding content type
    resource function post withMediaTypeOverrider() returns @http:Payload {mediaType: "application/json"} string {
        return "Hello, World!!";
    }

    //With different status code record
    resource function post pet3() returns OkString {
        OkString u = {
            body: "lna"
        };
        return u;
    }

    resource function post pet4() returns OkStringWithHeader {
        OkStringWithHeader u = {
            body: "lna",
            headers: {
                header1: "header1",
                header2: "header2"
            }
        };
        return u;
    }
}
