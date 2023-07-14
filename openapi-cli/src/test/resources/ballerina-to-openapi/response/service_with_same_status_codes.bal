import ballerina/http;
 public type Pet record {
    string name ;
    int id;
 };

 public type BadRequestRecord record {|
    *http:BadRequest;
    string body;
 |};

service /payloadV on new http:Listener(9090) {

    # A resource for generating greetings
    # + name - the input string name
    # + return - string name with hello message or error
    resource function get greeting(string name) returns string|http:Ok {
        return "Hello, " + name;
    }

    resource function post greeting() returns BadRequestRecord|http:BadRequest {
        return <http:BadRequest> {};
    }
}
