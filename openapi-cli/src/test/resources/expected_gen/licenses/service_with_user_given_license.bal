// Copyright (c) 2023 All Rights Reserved.

import ballerina/http;

listener http:Listener ep0 = new (80, config = {host: "petstore.openapi.io"});

service OASServiceType /v1 on ep0 {
    # List all pets
    #
    # + 'limit - How many items to return at one time (max 100)
    # + return - returns can be any of following types
    # Pets (An paged array of pets)
    # http:Response (unexpected error)
    resource function get pets(int? 'limit) returns Pets|http:Response {
    }
    # Create a pet
    #
    # + return - returns can be any of following types
    # http:Created (Null response)
    # http:Response (unexpected error)
    resource function post pets() returns http:Created|http:Response {
    }
    # Info for a specific pet
    #
    # + petId - The id of the pet to retrieve
    # + return - returns can be any of following types
    # Pets (Expected response to a valid request)
    # http:Response (unexpected error)
    resource function get pets/[string petId]() returns Pets|http:Response {
    }
}
