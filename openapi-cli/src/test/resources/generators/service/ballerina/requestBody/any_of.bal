import ballerina/http;

listener http:Listener ep0 = new (443, config = {host: "api.example.com"});

service / on ep0 {
    # List all pets
    #
    # + return - OK
    resource function post pets03(@http:Payload pets03_body payload) returns http:Created {
    }
}
