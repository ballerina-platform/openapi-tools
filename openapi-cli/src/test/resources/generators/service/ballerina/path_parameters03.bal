import ballerina/http;

listener http:Listener ep0 = new (9090, config = {host: "localhost"});

service / on ep0 {
    resource function get 'storage\-spaces/[string storageSpaceName]/keys/[string 'key]() returns string|http:BadRequest {
    }
}
