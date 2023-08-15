public type Pet record {
    int id?;
    string name;
    # pet status in the store
    string status?;
    string addedDate?;
    string points = "50";
};
