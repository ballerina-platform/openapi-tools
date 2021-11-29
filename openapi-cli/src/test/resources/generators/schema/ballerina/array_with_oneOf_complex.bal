public type WorldBankResponse (PaginationData|Indicator[]?)[];

public type PaginationData record {
    int page;
    int pages;
    int per_page;
    int total;
    string? sourceid;
    string? sourcename;
    string? lastupdated;
};

# Data indicator
public type Indicator record {
    # Id of the indicator
    string id;
    # Value represent by the indicator
    string value;
};
