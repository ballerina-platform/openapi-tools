public type Average float?;

public type Name string?;

# At least one entry should be non-null.
public type Scores decimal[]?;

public type Subject record{
    int id;
    string name;
    decimal[]? tag?;
};
