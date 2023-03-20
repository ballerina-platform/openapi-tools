import ballerina/http;

listener http:Listener ep0 = new (9090, config = {host: "localhost"});

service / on ep0 {
    # Get key value
    #
    # + storageSpaceName - name of the storage space
    # + 'key - name of the key to retrieve the value
    # + return - returns can be any of following types
    # string (successful operation)
    # http:BadRequest (Invalid key supplied)
    resource function get 'storage\-spaces/[string storageSpaceName]/keys/[string 'key]() returns string|http:BadRequest {
    }
}
