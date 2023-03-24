public type Additional_Primitive record {|
    string name?;
    anydata age?;
    string...;
|};

public type Additional_NestedArray record {|
    boolean isArray?;
    string[][]...;
|};

public type Additional_Array record {|
    boolean isArray?;
    string[]...;
|};
