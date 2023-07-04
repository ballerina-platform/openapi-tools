import ballerina/http;

listener http:Listener ep0 = new (80, config = {host: "petstore.openapi.io"});

service /v1 on ep0 {
    # List all pets
    #
    # + x\-request\-id - Unique request ID
    # + x\-registration\-id - Pet's registrationID
    # + x\-microchip\-id - Unique microchip ID
    # + x\-owner\-id - Pet's owner ID
    # + return - An array of pets
    resource function get pets(@http:Header int:Signed32 x\-request\-id, @http:Header int:Signed32 x\-registration\-id, @http:Header int:Signed32? x\-microchip\-id, @http:Header int? x\-owner\-id) returns http:Ok {
    }
}
