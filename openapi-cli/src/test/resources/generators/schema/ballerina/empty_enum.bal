public type MeetingObject record {
    # Meeting topic.
    string topic?;
    # Meeting Type: 1 - Instant meeting. 2 - Scheduled meeting. 3 - Recurring meeting with no fixed time. 8 - Recurring meeting with fixed time.
    int 'type?;
};

public type MeetingTypes string?;

# List of meetings
public type MeetingList record {
    # List of Meeting objects.
    MeetingObject[] meetings?;
};
