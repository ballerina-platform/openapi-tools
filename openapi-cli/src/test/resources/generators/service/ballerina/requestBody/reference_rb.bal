import ballerina/http;

listener http:Listener ep0 = new (9090, config = {host: "localhost"});

service /pizzashack/'1\.0\.0 on ep0 {
    resource function post 'order(@http:Payload Order payload) returns UnsupportedMediaTypeError|CreatedOrder|BadRequestError {
    }
}
