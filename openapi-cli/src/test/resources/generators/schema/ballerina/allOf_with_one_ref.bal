public type Activity record {
    # Unique identifier for the activity
    string uuid?;
};

public type Subject record {
    int id?;
    string name?;
    Activity subject_type?;
};
