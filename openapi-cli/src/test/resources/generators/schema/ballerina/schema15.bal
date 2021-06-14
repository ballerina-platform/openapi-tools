public type Activities record {
    # Position in pagination.
    int offset?;
    Activity[] history?;
};

public type User record {
    # First name of the Uber user.
    string first_name?;
    # Last name of the Uber user.
    string last_name?;
};

public type  AnyOF User|Activity;

public type Activity record {
    # Unique identifier for the activity
    string uuid?;
};
