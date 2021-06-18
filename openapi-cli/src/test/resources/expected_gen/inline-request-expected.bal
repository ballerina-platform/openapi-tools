import ballerina/http;
import ballerina/openapi;

listener http:Listener ep0 = new(9090);

@openapi:ServiceInfo {
    contract: "/var/folders/mz/xmjfm34s1n99v74_jtsbsdcw0000gn/T/openapi-cmd5033409960939893222/src/inlineModule/resources/inline-request-body.yaml"
}
service / on ep0 {

    resource function post user(http:Caller caller, http:Request req, @http:Payload record {  string userName; string
     userPhone; }  body) returns error? {

    }

}
