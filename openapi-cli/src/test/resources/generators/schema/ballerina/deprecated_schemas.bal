import ballerina/http;

@deprecated
public type Pets Pet[];

@deprecated
public type Owner record {
    string Name?;
    "admin"|"contributor"|"member"|"owner" Address?;
};

public type DogCreated record {|
    *http:Created;
    Dog body;
|};

public type ErrorDefault record {|
    *http:DefaultStatusCodeResponse;
    Error body;
|};

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
    @deprecated
    string tag?;
    string 'type?;
};
