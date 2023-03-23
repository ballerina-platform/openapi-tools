# Additional properties with type number
public type User05 record {|
    string? name?;
    int? id?;
    float...;
|};

# Additional properties with type number, nullable true
public type User16 record {|
    string? name?;
    int? id?;
    float?...;
|};

# Additional properties with reference
public type User06 record {|
    string? name?;
    int? id?;
    User01?...;
|};

# Additional properties with type object without properties
public type User07 record {|
    string? name?;
    int? id?;
    record {}?...;
|};

# Mock record
public type User record {
    string? name?;
    int? age?;
};

# Additional properties with object with property fields
public type User08 record {|
    string? name?;
    int? id?;
    record {string? country?; string? state?;}?...;
|};

# Additional properties with object with additional fields
public type User09 record {|
    string? name?;
    int? id?;
    record {}?...;
|};

# Additional properties with object with additional fields type string
public type User10 record {|
    string? name?;
    int? id?;
    record {|string?...;|}?...;
|};

# Additional properties with object with additional fields type with reference
public type User11 record {|
    string? name?;
    int? id?;
    record {|User?...;|}?...;
|};

# Additional properties with `true` enable
public type User01 record {
    string? name?;
    int? id?;
};

# Additional properties with `false` enable
public type User12 record {|
    string? name?;
    int? id?;
|};

# Additional properties with {}
public type User02 record {
    string? name?;
    int? id?;
};

# Free-form object
public type User13 record {
};

# Without additional properties
public type User03 record {
    string? name?;
    int? id?;
};

# Additional properties with object with additional fields type with inline object
public type User14 record {|
    string? name?;
    int? id?;
    record {|record {string? name?; string? place?;}?...;|}?...;
|};

# Additional properties with type string
public type User04 record {|
    string? name?;
    int? id?;
    string?...;
|};

# Additional properties with Array
public type User15 record {|
    string? name?;
    int? id?;
    string[]?...;
|};
