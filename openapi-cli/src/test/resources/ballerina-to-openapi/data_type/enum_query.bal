import ballerina/http;

listener http:Listener ep0 = new (9090);

service /api/v3 on ep0 {
    resource function get users/meetings/[MeetingGroup group](RoomNo required\-q\-ref, Status[] required\-q\-arr, Status[]? optional\-q\-arr, Format? format, "scheduled"|"live"|"upcoming" 'type = "live", RoomNo q\-default\-ref = "R5") returns MeetingList|http:NotFound {
        MeetingList m = {
            meetings: [
                {
                    topic: "My topic"
                }
            ]
        };
        return m;
    }
}
public enum MeetingGroup {
    ADMIN,
    HR,
    ENGINEERING
};

public enum Status {
    AVAILABLE,
    PENDING
}

public enum DateFormat {
    UTC,
    LOCAL,
    OFFSET,
    EPOCH
}

public enum TimeZone {
    IST,
    GMT,
    UTC
}

public enum Format {
    JSON,
    JSONP,
    MSGPACK
}

public enum RoomNo {
    R3,
    R5,
    R6
}

public type MeetingList record {
    Meeting[] meetings;
};

public type Meeting record {
    string topic;
};
