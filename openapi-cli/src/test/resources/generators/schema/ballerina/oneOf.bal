public type Activities record {
    # Position in pagination.
    int offset?;
    Activity[] history?;
};

public type Activity record {
    # Unique identifier for the activity
    string uuid?;
};

public type Error Activity|Profile01;

public type Profile01 record {
    # First name of the Uber user.
    string first_name?;
    # Last name of the Uber user.
    string last_name?;
};

public type Subject record {
    int id?;
    string name?;
    Activity|Profile01 subject_type?;
};
