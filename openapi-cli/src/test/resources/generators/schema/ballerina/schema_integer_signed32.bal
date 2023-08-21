import ballerina/constraint;

# A pet to be added to the store
public type Pet record {
    # Unique identifier for the pet
    int:Signed32 id;
    # Age of the pet in years
    @constraint:Int {minValue: 1}
    int:Signed32 age;
    # Weight of the pet in KG
    int:Signed32 weight?;
    # Height of the pet in CM
    @constraint:Int {maxValue: 10000}
    int:Signed32 height?;
    # Name of the pet
    string name;
    # Unique identifier of the microchip of the pet
    int microchipId?;
};
