
#
#+first_name-First name of the Uber user.
#+last_name-Last name of the Uber user.

public type User record {
    string first_name?;
    stringlast_name?;
    };
#
#+uuid-Unique identifier for the activity
public type Activity record {
    string uuid?;
};
#
#+offset-Position in pagination.
#+history-Field Description
public type Activities record {
    int offset?;
    Activity[]history?;
    }
#
public type AnyOFUser;|Activity;