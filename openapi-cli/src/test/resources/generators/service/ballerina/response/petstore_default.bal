import ballerina/http;

listener http:Listener ep0 = new (9090, config = {host: "localhost"});

service / on ep0 {
    # Description
    #
    # + payload - Created user object
    # + return - successful operation
    resource function post user(@http:Payload User payload) returns http:Response {
    }
    # Description
    #
    # + username - name that need to be deleted
    # + payload - Update an existent user in the store
    # + return - successful operation
    resource function put user/[string username](@http:Payload User payload) returns http:Response {
    }
    # Description
    #
    # + status - Status values that need to be considered for filter
    # + return - returns can be any of following types
    # http:BadRequest (Invalid status value)
    # http:Response (successful operation)
    resource function get pet/findByStatus(string status = "available") returns http:BadRequest|http:Response {
    }
    # Description
    #
    # + payload - parameter description
    # + return - returns can be any of following types
    # OkUser (Successful operation)
    # http:Response (successful operation)
    resource function post user/createWithList(@http:Payload User[] payload) returns OkUser|http:Response {
    }
    # Logs out current logged in user session
    #
    # + return - returns can be any of following types
    # User (Successful operation)
    # http:Response (successful operation)
    resource function get user/logout() returns User|http:Response {
    }
}
