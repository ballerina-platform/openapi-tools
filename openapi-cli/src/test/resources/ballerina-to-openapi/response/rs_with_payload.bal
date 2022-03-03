import ballerina/http;

listener http:Listener helloEp = new (9090);

type User record {
    int id;
    string name;
};

service /payloadV on helloEp {
    resource function get pet01() returns @http:Payload {mediaType: "application/xml"} User {
        User user = {
            id: 1,
            name: "abc"
        };
        return user;
    }
    resource function get pet02() returns @http:Payload {mediaType: "application/fake+xml"} json {
        return {};
    }
    resource function get pet03() returns @http:Payload {mediaType: "application/json"} string {
        return "";
    }
    resource function get .() returns @http:Payload {mediaType: ["application/json", "application/test+json"]} User {
        User user = {
            id: 1,
            name: "abc"
        };
        return user;
    }
}
