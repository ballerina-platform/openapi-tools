import ballerina/http;

listener http:Listener ep0 = new (443, config = {host: "api.sendgridf.com"});

service /v3 on ep0 {
    resource function get pets(int? 'limit) returns http:Ok {
    }
}
