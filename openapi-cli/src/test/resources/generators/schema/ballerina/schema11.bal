public type Pet record {
    int id;
    # name field
    string name;
    string tag?;
    record  {
        # type id
        string typeId?;
        string tagType?;
    } 'type?;
};
