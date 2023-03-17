import ballerina/http;

listener http:Listener ep0 = new (80, config = {host: "petstore.openapi.io"});

service /v1 on ep0 {
    # Creates a new pets.
    #
    # + return - A list of pets.
    resource function post pets() returns Pet[] {
    }
    # Creates a new pets.
    #
    # + return - A list of pets.
    resource function post pets02() returns string[] {
    }
    # Creates a new pets.
    #
    # + return - A list of pets.
    resource function post pets03() returns json {
    }
    # Creates a new pets.
    #
    # + return - A list of pets.
    resource function post pets04() returns json {
    }
}
