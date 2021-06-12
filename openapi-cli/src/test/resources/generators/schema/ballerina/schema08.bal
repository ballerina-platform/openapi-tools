#
# + id - Field Description
# + name - Field Description
# + tag - Field Description
# + 'type - Field Description
public type Pet record {
    int id;
    string name;
    string tag?;
    string 'type?;
};
#
# + petslist - Field Description
public type Pets record {
    Pet[] petslist;
};
