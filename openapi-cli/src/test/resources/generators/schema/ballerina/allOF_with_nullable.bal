public type JobBase JobCompact?;

# A response object returned from a batch request.
public type BatchResponse record {
    # The JSON body that the invoked endpoint returned.
    record {} body?;
    # A map of HTTP headers specific to this result. This is primarily used for returning a `Location` header to accompany a `201 Created` result.  The parent HTTP response will contain all common headers.
    record {} headers?;
    # The HTTP status code that the invoked endpoint returned.
    int? status_code?;
};

public type JobResponse JobBase?;

public type InlineResponse200 record {
    JobResponse? data?;
};

public type UserCompact record {
    # Users assigned to the task.
    string? abc?;
    # Users assigned to the task.
    string? bcd?;
};

public type TaskResponse record {
    UserCompact? assignee?;
};

public type JobCompact record {
    *UserCompact;
    # The subtype of this resource. Different subtypes retain many of the same fields and behavior, but may render differently in Asana or represent resources with different semantic meaning.
    string? resource_subtype?;
    # Status of job.
    string? status?;
};
