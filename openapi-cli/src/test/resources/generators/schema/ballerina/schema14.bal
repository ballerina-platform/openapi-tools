
public type Pet record {
    int id;
    string name;
    string tag?;
    record { string id?; string tagType?;} 'type?;
};
