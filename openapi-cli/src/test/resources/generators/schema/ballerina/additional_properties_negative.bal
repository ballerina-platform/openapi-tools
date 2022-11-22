# Additional properties with object with reference fields - this is issue https://github.com/swagger-api/swagger-parser/issues/1856
public type User01 record {|
    string? name?;
    int? id?;
    User03...;
|};

# Additional properties type Array with constraint. constraint wont support for rest filed in record.
public type User03 record {|
    string? name?;
    int? id?;
    string[]?...;
|};
