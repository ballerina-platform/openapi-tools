import ballerina/http;

service /payloadV on new http:Listener(9090) {

     resource function get path_with_request_body(@http:Payload string payload) {
     };

     resource function head path_with_request_body(@http:Payload string payload) {
     };

}
