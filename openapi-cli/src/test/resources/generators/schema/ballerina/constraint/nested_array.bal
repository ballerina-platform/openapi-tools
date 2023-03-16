import ballerina/constraint;

@constraint:Array {maxLength: 2, minLength: 1}
public type NestedarrayExamplesItemsArray NestedarrayexamplesitemsarrayItemsString[];

@constraint:String {minLength: 1}
public type NestedarrayexamplesitemsarrayItemsString string;

public type Nestedarray02ExamplesItemsArray Nestedarray02examplesitemsarrayItemsString[];

@constraint:String {minLength: 1}
public type Nestedarray02examplesitemsarrayItemsString string;

@constraint:Array {maxLength: 2, minLength: 1}
public type NoconstraintExamplesItemsArray int[];

# Every array items has constraint validation
public type NestedArray record {
    string name?;
    @constraint:Array {maxLength: 200, minLength: 2}
    NestedarrayExamplesItemsArray[] examples?;
};

# Some array items have constraint
public type NestedArray02 record {
    string name?;
    @constraint:Array {maxLength: 200, minLength: 2}
    Nestedarray02ExamplesItemsArray[] examples?;
};

# Last array item hasn't constraint values
public type NoConstraint record {
    string name?;
    @constraint:Array {maxLength: 200, minLength: 2}
    NoconstraintExamplesItemsArray[] examples?;
};
