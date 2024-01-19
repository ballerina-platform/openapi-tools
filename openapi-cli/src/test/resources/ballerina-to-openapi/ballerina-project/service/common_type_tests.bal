import ballerina/http;
import 'service.types as ty;

listener http:Listener helloEp = new (9090);

type User record {
    int id;
    string name;
};

service /payloadV on helloEp {
    resource function get pet01() returns @http:Payload {mediaType: ty:TEXT_HTML} User {
        User user = {
            id: 1,
            name: "abc"
        };
        return user;
    }

    resource function get expense(@http:Query ty:Currency currency) returns int? {
    }

    resource function get bill(ty:Product p) {
    }
}
