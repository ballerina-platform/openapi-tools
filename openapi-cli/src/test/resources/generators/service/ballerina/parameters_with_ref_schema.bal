import ballerina/http;

listener http:Listener ep0 = new (443, config = {host: "petstore3.swagger.io"});

service /api/v3 on ep0 {
    # List all the meetings that were scheduled
    #
    # + location - Meeting location
    # + types - Meeting Types
    # + audience - Meeting audience
    # + timezone - Meeting timezone
    # + return - returns can be any of following types
    # MeetingList (HTTP Status Code:200. List of meetings returned.)
    # http:NotFound (HTTP Status Code:404 User ID not found. Error Code:1001, User not exist or not belong to this account.)
    resource function get users/meetings(MeetingTypes[] types, Audience? audience, TimeZone? timezone, RoomNo location = "R5") returns MeetingList|http:NotFound {
    }
}
