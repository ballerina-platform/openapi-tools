import ballerina/http;

listener http:Listener ep0 = new (443, config = {host: "petstore3.swagger.io"});

service /api/v3 on ep0 {
    # Add a new pet to the store
    #
    # + id - parameter description
    # + name - parameter description
    # + payload - Create a new pet in the store
    # + return - Successful operation
    resource function post pet(int id, @http:Payload Pet payload, string name = "doggie") returns OkPet {
    }
}
