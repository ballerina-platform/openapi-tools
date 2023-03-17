import ballerina/http;

listener http:Listener ep0 = new (443, config = {host: "api.sendgridf.com"});

service /v3 on ep0 {
    # List all pets
    #
    # + 'limit - How many items to return at one time (max 100)
    # + return - An paged array of pets
    resource function get pets(int? 'limit) returns http:Ok {
    }
}
