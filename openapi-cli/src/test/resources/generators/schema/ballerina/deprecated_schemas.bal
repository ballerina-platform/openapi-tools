import ballerina/http;

@deprecated
public type Pets Pet[];

public type CreatedDog record {|
    *http:Created;
    Dog body;
    map<string|string[]> headers;
|};

@deprecated
public type Owner record {
    string Name?;
    "admin"|"contributor"|"member"|"owner" Address?;
};

public type Error record {
    int code;
    string message;
};

public type Dog record {
    *Pet;
    boolean bark?;
    *Owner;
};

# Pet Object
#
# # Deprecated
# Pet object is deprecated from version 2
@deprecated
public type Pet record {
    int id;
    string name;
    string tag?;
    string 'type?;
};
