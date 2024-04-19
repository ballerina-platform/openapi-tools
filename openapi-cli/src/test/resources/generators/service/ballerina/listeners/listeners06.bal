import ballerina/http;

listener http:Listener ep0 = new (9090, config = {host: "localhost"});

service / on ep0 {
    # + return - Accepted
    resource function get path() returns http:Accepted {
    }
}
