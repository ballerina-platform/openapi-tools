import ballerina/http;
import ballerina/mime;

listener http:Listener helloEp = new (9090);

type AcceptedResponse record {|
    *http:Accepted;
    string mediaType = mime:APPLICATION_FORM_URLENCODED;
    map<string> body;
|};

service /payloadV on helloEp {
    resource function get accepted() returns AcceptedResponse {
        // implement logic
        return {
            body: {
                "message": "Request is accepted by the server"
            }
        };
    }
    resource function get foo() returns @http:Payload {mediaType: mime:APPLICATION_FORM_URLENCODED} map<string> {
        map<string> ms = {
            "x": "abc",
            "y": "cdf"
        };
        return ms;
    }
    resource function get bar() returns @http:Payload map<string> {
        map<string> ms = {
            "x": "abc",
            "y": "cdf"
        };
        return ms;
    }
    resource function get barint() returns @http:Payload map<int> {
        map<int> ms = {
            "x": 1,
            "y": 2
        };
        return ms;
    }
}
