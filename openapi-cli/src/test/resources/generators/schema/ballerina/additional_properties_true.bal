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

public type Store_inventory_body record {
    User? user?;
    User01? user1?;
    User02? user2?;
    User03? user3?;
    User04? user4?;
    User05? user5?;
    User06? use6?;
    User07? user7?;
    User08? user8?;
    User09? use9?;
    User10? user10?;
    User11? user11?;
    User12? user12?;
    User13? user13?;
    User14? user14?;
    User15? user15?;
    User16? user16?;
};

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
