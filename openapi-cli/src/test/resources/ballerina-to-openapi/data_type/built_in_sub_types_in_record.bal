import ballerina/http;

public type BalSignedInts record {|
    int:Signed32 signed32;
    int:Signed16 signed16;
    int:Signed8 signed8;
|};

public type BalUnsignedInts record {|
    int:Unsigned32 unsigned32;
    int:Unsigned16 unsigned16;
    int:Unsigned8 unsigned8;
|};

public type BalInts record {|
    BalSignedInts signed;
    BalUnsignedInts unsigned;
|};

public type BalXmls record {|
    xml:Comment comment;
    xml:Element element;
    xml:ProcessingInstruction processingInstruction;
    xml:Text text;
|};

public type BalSubTypes record {|
    string:Char char;
    BalInts ints;
    BalXmls xmls;
|};

service /payloadV on new http:Listener(9090) {

    resource function get path() returns BalSubTypes {
        return {
            char: "a",
            ints: {
                signed: {
                    signed32: 32,
                    signed16: 16,
                    signed8: 8
                },
                unsigned: {
                    unsigned32: 32,
                    unsigned16: 16,
                    unsigned8: 8
                }
            },
            xmls: {
                comment: xml`<!-- comment -->`,
                element: xml`<element>element</element>`,
                processingInstruction: xml`<?processing instruction?>`,
                text: xml`text`
            }
        };
    }
}
