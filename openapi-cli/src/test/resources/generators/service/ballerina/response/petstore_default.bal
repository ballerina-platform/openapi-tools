import ballerina/http;

listener http:Listener ep0 = new (9090, config = {host: "localhost"});

service / on ep0 {
    # Description
    #
    # + status - Status values that need to be considered for filter
    # + return - returns can be any of following types
    # http:BadRequest (Invalid status value)
    # http:DefaultStatusCodeResponse (successful operation)
    resource function get pet/findByStatus("available"|"pending"|"sold" status = "available") returns http:BadRequest|http:DefaultStatusCodeResponse {
    }
    # Logs out current logged in user session
    #
    # + return - returns can be any of following types
    # http:Ok (Successful operation)
    # http:DefaultStatusCodeResponse (successful operation)
    resource function get user/logout() returns User|UserDefault {
    }
    # Description
    #
    # + payload - Created user object
    # + return - successful operation
    resource function post user(@http:Payload User payload) returns UserXmlDefault {
    }
    # Description
    #
    # + payload - parameter description
    # + return - returns can be any of following types
    # http:Ok (Successful operation)
    # http:DefaultStatusCodeResponse (successful operation)
    resource function post user/createWithList(@http:Payload User[] payload) returns UserOk|http:DefaultStatusCodeResponse {
    }
    # Description
    #
    # + username - name that need to be deleted
    # + payload - Update an existent user in the store
    # + return - successful operation
    resource function put user/[string username](@http:Payload User payload) returns http:DefaultStatusCodeResponse {
    }
}
