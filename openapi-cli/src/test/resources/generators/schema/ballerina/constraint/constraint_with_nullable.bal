public type Average float?;

# At least one entry should be non-null.
public type Scores decimal[]?;

public type Subject record{
    int id;
    string name;
    decimal[]? tag?;
};

public type Name string?;
