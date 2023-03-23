public type ListObject record {
};

public type UserPlayListDetails record {
    # A link to the Web API endpoint returning the full result of the request
    string? href?;
    # The requested data.
    ListObject[]? items?;
    # The maximum number of items in the response (as set in the query or by default).
    int? 'limit?;
    # URL to the next page of items. ( `null` if none)
    string next?;
    # The offset of the items returned (as set in the query or by default)
    int? offset?;
    # URL to the previous page of items. ( `null` if none) //anydata
    anydata previous?;
    ListObject total?;
};
