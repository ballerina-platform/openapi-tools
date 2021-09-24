import ballerina/http;

listener http:Listener ep0 = new(9090);

service  / on ep0 {

    resource function get ping(string filter) returns json {

   }
}
