import ballerina/http;

listener http:Listener ep0 = new (443, config = {host: "petstore.swagger.io"});

service /v2 on ep0 {
    # Update an existing pet
    #
    # + payload - Pet object that needs to be added to the store
    # + return - Invalid ID supplied or Pet not found or Validation exception
    resource function put pet(@http:Payload {mediaType: ["application/json", "application/xml"]} Pet payload) returns http:BadRequest|http:NotFound|http:MethodNotAllowed {
    }
    # Add a new pet to the store
    #
    # + payload - Pet object that needs to be added to the store
    # + return - Invalid input
    resource function post pet(@http:Payload {mediaType: ["application/json", "application/xml"]} Pet payload) returns http:MethodNotAllowed {
    }
    # Finds Pets by status
    #
    # + status - Status values that need to be considered for filter
    # + return - successful operation or Invalid status value
    resource function get pet/findByStatus(string[] status) returns Pet[]|http:BadRequest {
    }
    # Finds Pets by tags
    #
    # + tags - Tags to filter by
    # + return - successful operation or Invalid tag value
    resource function get pet/findByTags(string[] tags) returns Pet[]|http:BadRequest {
    }
    # Find pet by ID
    #
    # + petId - ID of pet to return
    # + return - successful operation or Invalid ID supplied or Pet not found
    resource function get pet/[int petId]() returns Pet|http:BadRequest|http:NotFound {
    }
    # Updates a pet in the store with form data
    #
    # + petId - ID of pet that needs to be updated
    # + payload - request payload
    # + return - Invalid input
    resource function post pet/[int petId](@http:Payload {mediaType: "application/x-www-form-urlencoded"} Pet_petId_body payload) returns http:MethodNotAllowed {
    }
    # Deletes a pet
    #
    # + petId - Pet id to delete
    # + return - Invalid ID supplied or Pet not found
    resource function delete pet/[int petId](@http:Header string? api_key) returns http:BadRequest|http:NotFound {
    }
    # Uploads an image
    #
    # + petId - ID of pet to update
    # + request - request payload
    # + return - successful operation
    resource function post pet/[int petId]/uploadImage(http:Request request) returns OkApiResponse {
    }
    # Returns pet inventories by status
    #
    # + return - successful operation
    resource function get store/inventory() returns StoreInventoryResponse {
    }
    # Place an order for a pet
    #
    # + request - order placed for purchasing the pet
    # + return - successful operation or Invalid Order
    resource function post store/'order(http:Request request) returns OkOrder|http:BadRequest {
    }
    # Find purchase order by ID
    #
    # + orderId - ID of pet that needs to be fetched
    # + return - successful operation or Invalid ID supplied or Order not found
    resource function get store/'order/[int orderId]() returns Order|http:BadRequest|http:NotFound {
    }
    # Delete purchase order by ID
    #
    # + orderId - ID of the order that needs to be deleted
    # + return - Invalid ID supplied or Order not found
    resource function delete store/'order/[int orderId]() returns http:BadRequest|http:NotFound {
    }
    # Create user
    #
    # + request - Created user object
    # + return - successful operation
    resource function post user(http:Request request) returns http:Response {
    }
    # Creates list of users with given input array
    #
    # + request - List of user object
    # + return - successful operation
    resource function post user/createWithArray(http:Request request) returns http:Response {
    }
    # Creates list of users with given input array
    #
    # + request - List of user object
    # + return - successful operation
    resource function post user/createWithList(http:Request request) returns http:Response {
    }
    # Logs user into the system
    #
    # + username - The user name for login
    # + password - The password for login in clear text
    # + return - successful operation or Invalid username/password supplied
    resource function get user/login(string username, string password) returns string|http:BadRequest {
    }
    # Logs out current logged in user session
    #
    # + return - successful operation
    resource function get user/logout() returns http:Response {
    }
    # Get user by user name
    #
    # + username - The name that needs to be fetched. Use user1 for testing.
    # + return - successful operation or Invalid username supplied or User not found
    resource function get user/[string username]() returns User|http:BadRequest|http:NotFound {
    }
    # Updated user
    #
    # + username - name that need to be updated
    # + request - Updated user object
    # + return - Invalid user supplied or User not found
    resource function put user/[string username](http:Request request) returns http:BadRequest|http:NotFound {
    }
    # Delete user
    #
    # + username - The name that needs to be deleted
    # + return - Invalid username supplied or User not found
    resource function delete user/[string username]() returns http:BadRequest|http:NotFound {
    }
}
