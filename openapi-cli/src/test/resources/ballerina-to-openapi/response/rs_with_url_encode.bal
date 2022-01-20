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
}
