import ballerina/http;

listener http:Listener ep0 = new (9090);

service /payloadV on ep0 {
    resource function get users/meetings/[MeetingGroup group](@http:Header DateFormat 'x\-date\-required, @http:Header DateFormat? 'x\-date\-optional, @http:Header TimeZone[] 'x\-required\-arr, @http:Header TimeZone[]? 'x\-optional\-arr, @http:Header DateFormat[] x\-def\-arr = ["UTC"], @http:Header DateFormat x\-def\-header= "UTC" ) returns MeetingList|http:NotFound {
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
