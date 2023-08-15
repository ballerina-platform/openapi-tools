import ballerina/http;


type Patient record {|
    Extension[] ext?;
    string id?;
    Reference ref?;
|};

public type Extension ExtensionExtension|StringExtension|CodingExtension|CodeExtension|IntegerExtension;

public type ExtensionExtension record {|
    *Element;
    Extension[] extension?;
|};

public type StringExtension record {|
    uri url;
    string valueString;
|};

public type uri string;
public type CodingExtension record {|
    uri url;
    Coding valueCoding;
|};

public type Coding record {|
    *Element;

    string id?;
    Extension[] extension?;
    uri system?;
    string 'version?;
    string display?;
    boolean userSelected?;
|};

public type CodeExtension record {|
    uri url;
    string valueCode;
|};

public type IntegerExtension record {|
    uri url;
    int valueInteger;
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
    Patient {
        Patient patient = {
            id: id
        };

        return patient;
    }
}
