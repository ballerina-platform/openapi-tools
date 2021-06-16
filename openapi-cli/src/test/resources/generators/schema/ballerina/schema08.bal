public type Pets Pet[];

public type Pet record {
    int id;
    string name;
    string tag?;
    string 'type?;
};
