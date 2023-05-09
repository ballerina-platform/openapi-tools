public type MeetingObject record {
    # Meeting topic.
    string topic?;
    # Meeting Type: 1 - Instant meeting. 2 - Scheduled meeting. 3 - Recurring meeting with no fixed time. 8 - Recurring meeting with fixed time.
    1|2|3|8 'type?;
    MeetingTypes broadcastType?;
    # Status values that need to be considered for filter
    ("available"|"pending")[] status?;
    true|false isLive?;
    120.5|300.3 averageAudioLength?;
    300|100? participants?;
};

public type MeetingTypes "scheduled"|"live"|"upcoming";

# List of meetings
public type MeetingList record {
    # List of Meeting objects.
    MeetingObject[] meetings?;
};
