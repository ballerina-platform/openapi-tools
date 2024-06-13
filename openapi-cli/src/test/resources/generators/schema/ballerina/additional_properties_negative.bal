import ballerina/http;

public type User04Accepted record {|
    *http:Accepted;
    User04 body;
|};

# Additional properties with object with reference fields - this is issue https://github.com/swagger-api/swagger-parser/issues/1856
public type User01 record {|
    string? name?;
    int? id?;
    User03?...;
|};

# Additional properties type Array with constraint. constraint won't support for rest filed in record.
public type User03 record {|
    string? name?;
    int? id?;
    string[]?...;
|};

public type User03Created record {|
    *http:Created;
    User03 body;
|};

# These Additional properties are complex to map.
public type User04 record {
    string? name?;
    int? id?;
};
