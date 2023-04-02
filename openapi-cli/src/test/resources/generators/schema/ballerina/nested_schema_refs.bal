public type ProjectStatusBase record {
    *ProjectStatusCompact;
    UserCompact author?;
    # The time at which this project status was last modified.
    anydata modified_at?;
    # The text content of the status update.
    string text;
    string html_text?;
    # The color associated with the status update.
    string color;
};

public type ProjectStatusRequest ProjectStatusBase;

public type UserCompact record {
    *AsanaResource;
    # Read-only except when same user as requester.
    string name?;
};

public type Project_gid_project_statuses_body record {
    ProjectStatusRequest data?;
};

# A generic Asana Resource, containing a globally unique identifier.
public type AsanaResource record {
    # Globally unique identifier of the resource, as a string.
    string gid?;
    # The base type of this resource.
    string resource_type?;
};

public type ProjectStatusCompact record {
    *AsanaResource;
    # The title of the project status update.
    string title?;
};
