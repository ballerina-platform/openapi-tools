import ballerina/constraint;

public type Category record {
    # Unique identifier for the category
    int:Signed32 id?;
    # Name of the category
    string name?;
};

# Unique identifier for the pet
@constraint:Int {minValue: 1}
public type PetId int:Signed32;

public type Pet record {
    # Unique identifier for the pet
    PetId id;
    # Age of the pet in years
    int:Signed32 age;
    # Weight of the pet in KG
    int:Signed32 weight?;
    # Height of the pet in CM
    @constraint:Int {maxValue: 10000}
    int height?;
    # Name of the pet
    string name;
    Category category?;
};
