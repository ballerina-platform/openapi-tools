import ballerina/http;

# Adding doc
public type Action string;
public type Count int;
public type Rels string[];
public type Books map<string>;
public type Price float|PriceMap;
public type Salary int|float|decimal;
public type PetId int;

type Link record {|
    Rels rels;
    Action actions?;
    Count? count;
    Books books?;
    Price price;
    Salary salary;
|};

type PriceMap record {|
    int price;
    decimal salary;
|};

service /payloadV on new http:Listener(9090) {

    # Represents Snowpeak reservation resource
    #
    # + link - Reservation representation
    resource function post reservation(@http:Payload Link link) {
    }
    // Current http module doesn't support define datatype
    // resource function get reservation(Count count) {
    // }

    resource function get reservation/[PetId id]() {
    }
}
