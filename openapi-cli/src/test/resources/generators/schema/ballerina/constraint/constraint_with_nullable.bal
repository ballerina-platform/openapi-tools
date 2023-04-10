# At least one entry should be non-null.
public type Scores decimal[]?;

public type Average float?;

public type Pet record {
    int id;
    string name;
    decimal[]? tag?;
};

public type Name string?;
