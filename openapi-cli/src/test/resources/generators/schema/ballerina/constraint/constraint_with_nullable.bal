# At least one entry should be non-null.
public type NullableNumberInterval decimal[]?;

public type Pet record {
    int id;
    string name;
    decimal[]? tag?;
};
