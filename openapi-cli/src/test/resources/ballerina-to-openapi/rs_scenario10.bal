import ballerina/http;
import ballerina/log;

listener http:Listener helloEp = new (9090);
type  Pet record  {
    int  id;
    string  name;
    string  tag?;
    string  'type?;
};

service /payloadV on helloEp {
    resource function post hi01(http:Caller caller, http:Request request) returns Pet[] {
    }
    resource function post hi(http:Caller caller, http:Request request) returns string[] {}
}
