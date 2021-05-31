public type Pet record {
    int id;
    string name;
    string tag?;
    string 'type?;
};

public type Pets record {
    Pet[] petslist;
};
