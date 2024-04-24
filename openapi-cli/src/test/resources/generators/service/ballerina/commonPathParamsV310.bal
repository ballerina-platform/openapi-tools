import ballerina/http;

listener http:Listener ep0 = new (9090, config = {host: "localhost"});

service / on ep0 {
    # Updates a pet in the store with form data
    #
    # + name - Name of pet that needs to be updated
    # + petId - ID of pet to return
    # + return - Invalid input
    resource function post pet/[int petId](string? name) returns http:MethodNotAllowed {
    }
}
