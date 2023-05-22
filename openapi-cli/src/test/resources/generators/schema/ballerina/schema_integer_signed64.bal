import ballerina/constraint;

public type Pet record {
    # Unique identifier for the pet
    int:Signed32 id;
    # Name of the pet
    string name;
    # Unique identifier for the owner
    int ownerId;
    # Unique identifier of the microchip of the pet
    @constraint:Int {minValue: 500}
    int microchipId;
    # Unique identifier of the insurance policy of the pet
    int insurancePolicyId?;
    # Gov registration number of the pet
    @constraint:Int {minValue: 1000}
    int registrationNumber?;
};
