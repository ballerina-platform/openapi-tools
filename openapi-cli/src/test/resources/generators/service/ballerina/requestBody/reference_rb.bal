import ballerina/http;

listener http:Listener ep0 = new (9090, config = {host: "localhost"});

service /pizzashack/'1\.0\.0 on ep0 {
    # Create a new Order
    #
    # + payload - Order object that needs to be added
    # + return - returns can be any of following types
    # Order (Created. Successful response with the newly created object as entity inthe body.Location header contains URL of newly created entity.)
    # BadRequestError (Bad Request. Invalid request or validation error.)
    # UnsupportedMediaTypeError (Unsupported Media Type. The entity of the request was in a not supported format.)
    resource function post 'order(@http:Payload Order payload) returns Order|BadRequestError|UnsupportedMediaTypeError {
    }
}
