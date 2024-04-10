import ballerina/http;

listener http:Listener ep0 = new (443, config = {host: "petstore3.swagger.io"});

service /api/v3 on ep0 {
# Deletes a pet
    #
    # + api_key -
    # + petId - Pet id to delete
    # + return - Invalid pet value
    resource function delete pet/[int petId](http:Caller caller, http:Request request) returns error? {
    }
    resource function delete store/'order/[int orderId](http:Caller caller, http:Request request) returns error? {
    }
    # Delete user
    #
    # + username - The name that needs to be deleted
    # + return - returns can be any of following types
    # http:BadRequest (Invalid username supplied)
    # http:NotFound (User not found)
    resource function delete user/[string username](http:Caller caller, http:Request request) returns error? {
    }
    # Find pet by ID
    #
    # + petId - ID of pet to return
    # + return - returns can be any of following types
    # http:Ok (successful operation)
    # http:BadRequest (Invalid ID supplied)
    # http:NotFound (Pet not found)
    resource function get pet/[int petId](http:Caller caller, http:Request request) returns error? {
    }
    # Finds Pets by status
    #
    # + status - Status values that need to be considered for filter
    # + return - returns can be any of following types
    # http:Ok (successful operation)
    # http:BadRequest (Invalid status value)
    resource function get pet/findByStatus(http:Caller caller, http:Request request) returns error? {
    }
    # Finds Pets by tags
    #
    # + tags - Tags to filter by
    # + return - returns can be any of following types
    # http:Ok (successful operation)
    # http:BadRequest (Invalid tag value)
    resource function get pet/findByTags(http:Caller caller, http:Request request) returns error? {
    }
    resource function get store/'order/[int orderId](http:Caller caller, http:Request request) returns error? {
    }
    # Returns pet inventories by status
    #
    # + return - successful operation
    resource function get store/inventory(http:Caller caller, http:Request request) returns error? {
    }
    # Get user by user name
    #
    # + username - The name that needs to be fetched. Use user1 for testing.
    # + return - returns can be any of following types
    # http:Ok (successful operation)
    # http:BadRequest (Invalid username supplied)
    # http:NotFound (User not found)
    resource function get user/[string username](http:Caller caller, http:Request request) returns error? {
    }
    # Logs user into the system
    #
    # + username - The user name for login
    # + password - The password for login in clear text
    # + return - returns can be any of following types
    # http:Ok (successful operation)
    # http:BadRequest (Invalid username/password supplied)
    resource function get user/login(http:Caller caller, http:Request request) returns error? {
    }
    # Logs out current logged in user session
    #
    # + return - successful operation
    resource function get user/logout(http:Caller caller, http:Request request) returns error? {
    }
    # Add a new pet to the store
    #
    # + payload - Create a new pet in the store
    # + return - returns can be any of following types
    # http:Ok (Successful operation)
    # http:MethodNotAllowed (Invalid input)
    resource function post pet(http:Caller caller, http:Request request) returns error? {
    }
    # Updates a pet in the store with form data
    #
    # + petId - ID of pet that needs to be updated
    # + name - Name of pet that needs to be updated
    # + status - Status of pet that needs to be updated
    # + return - Invalid input
    resource function post pet/[int petId](http:Caller caller, http:Request request) returns error? {
    }
    # uploads an image
    #
    # + petId - ID of pet to update
    # + additionalMetadata - Additional Metadata
    # + return - successful operation
    resource function post pet/[int petId]/uploadImage(http:Caller caller, http:Request request) returns error? {
    }
    resource function post store/'order(http:Caller caller, http:Request request) returns error? {
    }
    # Create user
    #
    # + payload - Created user object
    # + return - successful operation
    resource function post user(http:Caller caller, http:Request request) returns error? {
    }
    # Creates list of users with given input array
    #
    # + return - returns can be any of following types
    # http:Ok (Successful operation)
    # http:Response (successful operation)
    resource function post user/createWithList(http:Caller caller, http:Request request) returns error? {
    }
    # Update an existing pet
    #
    # + payload - Update an existent pet in the store
    # + return - returns can be any of following types
    # http:Ok (Successful operation)
    # http:BadRequest (Invalid ID supplied)
    # http:NotFound (Pet not found)
    # http:MethodNotAllowed (Validation)
    resource function put pet(http:Caller caller, http:Request request) returns error? {
    }
    # Update user
    #
    # + username - name that need to be deleted
    # + payload - Update an existent user in the store
    # + return - successful operation
    resource function put user/[string username](http:Caller caller, http:Request request) returns error? {
    }
}
