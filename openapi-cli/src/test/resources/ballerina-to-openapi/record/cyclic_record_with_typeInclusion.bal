import ballerina/http;

type Patient record {|
    string id?;
    Reference ref?;
|};

public type Reference record {|
    *Element;
    string reference?;
    string 'type?;
    Identifier identifier?;
    string display?;
|};

public type Identifier record {|
    *Element;
    string value?;
    Reference assigner?;
|};

public type Element record {|
    string id?;
    int element?;
|};


service /payloadV on new http:Listener(9090) {

    // Read the current state of the resource represented by the given id.
    isolated resource function get fhir/r4/Patient/[string id]()
    returns @http:Payload {mediaType: ["application/fhir+json", "application/fhir+xml"]}
    Patient|xml {
        Patient patient = {
            id: id
        };

        return patient;
    }
}
