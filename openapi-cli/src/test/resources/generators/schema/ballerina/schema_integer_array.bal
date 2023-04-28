import ballerina/constraint;

@constraint:Int{minValue:1}
public type PetAgeItemsInteger int:Signed32;

public type Pet_name record {
  int:Signed32 id?;
  string name?;
};

# A pet to be added to the store
public type Pet record {
  # Unique identifier for the pet
  int:Signed32 id;
  # Age of the pet in years
  PetAgeItemsInteger[] age;
  # Weight of the pet in KG
  int:Signed32 weight?;
  # Height of the pet in CM
  int:Signed32[][]height?;
  # Name of the pet
  Pet_name[] name;
  # Unique identifier of the microchip of the pet
  int microchipId?;
};
