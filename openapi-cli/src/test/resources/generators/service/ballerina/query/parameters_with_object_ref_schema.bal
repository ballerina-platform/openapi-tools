import ballerina/http;

listener http:Listener ep0 = new (443, config = {host: "petstore3.swagger.io"});

service /api/v3 on ep0 {
    # List all the meetings that were scheduled
    #
    # + location - Meeting location
    # + return - HTTP Status Code:200. List of meetings returned.
    resource function get users/meetings(Room? location) returns MeetingList {
    }
}
