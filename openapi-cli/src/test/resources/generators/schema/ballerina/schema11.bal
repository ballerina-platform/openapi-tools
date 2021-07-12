public type Pet record {
    int id;
    # name field
    string name;
    string tag?;
    PetType 'type?;
};

public type PetType record {
    # type id
    string typeId?;
    string tagType?;
};
