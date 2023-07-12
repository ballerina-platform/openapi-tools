import ballerina/http;

listener http:Listener ep0 = new (443, config = {host: "petstore3.swagger.io"});

service /api/v3 on ep0 {
    # List meetings
    #
    # + 'type - The meeting types. Scheduled, live or upcoming
    # + status - Status values that need to be considered for filter
    # + x\-date\-format - Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) & [leettime.de](http://leettime.de/))
    # + location - Meeting location
    # + format - The response format you would like
    # + return - returns can be any of following types
    # MeetingList (HTTP Status Code:200. List of meetings returned.)
    # http:NotFound (HTTP Status Code:404 User ID not found. Error Code:1001, User not exist or not belong to this account.)
    resource function get users/meetings("scheduled"|"live"|"upcoming"? 'type, ("available"|"pending"?)[]? status, @http:Header "UTC"|"LOCAL"|"OFFSET"|"EPOCH"|"LEET"? x\-date\-format, "json"|"jsonp"|"msgpack"|"html"? format, RoomNo location = "R5") returns MeetingList|http:NotFound {
    }
}
