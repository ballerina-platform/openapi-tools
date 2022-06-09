import ballerina/http;

public type Action string?;
public type Count decimal?;
public type Rels string[]?;
public type Books map<string>?;

type Link record {|
    Rels rels;
    Action actions;
    Count count?;
    Books books;
|};

service /payloadV on new http:Listener(9090) {

    # Represents Snowpeak reservation resource
    #
    # + link - Reservation representation
    resource function post reservation(@http:Payload Link link) {
    }
}
