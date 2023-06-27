import ballerina/http;

listener http:Listener ep0 = new (443, config = {host: "petstore3.swagger.io"});

service /api/v3 on ep0 {
    # List all the meetings that were scheduled
    #
    # + organizer - Meeting organizer
    # + location - Meeting location
    # + types - Meeting Types
    # + audience - Meeting audience
    # + remarks - Meeting remarks
    # + invoices - Booking Invoice
    # + return - returns can be any of following types
    # MeetingList (HTTP Status Code:200. List of meetings returned.)
    # http:NotFound (HTTP Status Code:404 User ID not found. Error Code:1001, User not exist or not belong to this account.)
    resource function get users/meetings(Organizer organizer, MeetingTypes[] types, Audience? audience, map<json> remarks, Booking_invoice[] invoices, RoomNo location = "R5") returns MeetingList|http:NotFound {
    }
}
