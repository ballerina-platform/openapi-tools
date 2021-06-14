public type UserPlayListDetails record {
    string href?;
    ListObject[] items?;
    int 'limit?;
    string? next?;
    int offset?;
    anydata? previous?;
    ListObject total?;
};

public type ListObject record {
};
