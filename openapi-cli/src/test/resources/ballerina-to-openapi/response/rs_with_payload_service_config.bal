import ballerina/mime;
import ballerina/http;

listener http:Listener helloEp = new (9090);

type User record {
    int id;
    string name;
};

@http:ServiceConfig{
    mediaTypeSubtypePrefix: "snowflake"
}
service /payloadV on helloEp {
    resource function get pet01() returns @http:Payload {mediaType: mime:APPLICATION_JSON} User {
        User user = {
            id: 1,
            name: "abc"
        };
        return user;
    }
    resource function get pet02() returns @http:Payload {mediaType: "application/xml"} json {
        return {};
    }
    resource function get pet03() returns @http:Payload {mediaType: ["application/json", "text/plain"]} string {
        return "";
    }
}
