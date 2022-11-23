# Multiple additional fields , result can not have multiple field
public type User05 record {|
    *Pet;
    string? name?;
    int? id?;
    int|string[]?...;
|};

# Additional properties with type object without properties
public type User06 record {|
    *Pet;
    string? name?;
    int? id?;
    record {}...;
|};

# Additional properties with object with property fields
public type User07 record {|
    *Pet;
    string? name?;
    int? id?;
    record {|string? country?; string? state?;|}...;
|};

# Without any additional field it maps to closed record.
public type User01 record {|
    *Pet;
    string? name?;
    int? id?;
|};

# Additional properties with `true` enable
public type User02 record {
    *Pet;
    string? name?;
    int? id?;
};

# Mock record
public type Pet record {|
    string? name?;
    int? age?;
|};

# Additional properties with {}
public type User03 record {
    *Pet;
    string? name?;
    int? id?;
};

# Additional properties with type string
public type User04 record {|
    *Pet;
    string? name?;
    int? id?;
    string...;
|};
