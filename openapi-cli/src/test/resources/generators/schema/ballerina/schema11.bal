public type Pet_type record {
    # type id
    string typeId?;
    string tagType?;
};

public type Pet record {
    int id;
    # name field
    string name;
    string tag?;
    Pet_type 'type?;
};
