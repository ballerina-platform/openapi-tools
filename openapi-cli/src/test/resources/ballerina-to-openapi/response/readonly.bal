import ballerina/http;

type Pet record {
    string name;
};

public type ReadOnlyStr readonly & string;

public type ReadOnlyPet readonly & Pet;

public type ReadOnlyStrNil readonly & string?;

public type ReadOnlyStrArray readonly & string[];

public type ReadOnlyMapJson readonly & map<json>;

public type ReadOnlyJson readonly & json;

public type ReadOnlyXML readonly & xml;

public type ReadOnlyError readonly & error;

public type ReadOnlyPrimitiveUnion readonly & string|int;

public type ReadOnlyBracedUnion readonly & (string|int);

public type ReadOnlyRec readonly & record {|
    readonly string id;
    string name;
|};

service /payloadV on new http:Listener(9090) {

    resource function get res1(string id) returns ReadOnlyPet {
        return {
            name: "Kitty"
        };
    }

    resource function get res2(string id) returns ReadOnlyStr {
        return "Value";
    }

    resource function get res3(string id) returns ReadOnlyStrNil {
        return ();
    }

    resource function get res4(string id) returns ReadOnlyStrArray {
        ReadOnlyStrArray values = ["val1", "val2"];
        return values;
    }


    resource function get res5(string id) returns ReadOnlyMapJson {
        return {"map": {}};
    }

    resource function get res6(string id) returns ReadOnlyJson {
        return {
            "name": "test-user"
        };
    }

    resource function get res7(string id) returns ReadOnlyXML {
        ReadOnlyXML x1 = xml `<book>The Lost World</book>`;
        return x1;
    }

    // need to discuss
    resource function get res8(string id) returns ReadOnlyError {
        return error("Value");
    }

    resource function get res9(string id) returns ReadOnlyPrimitiveUnion {
        return "value";
    }


    resource function get res10(string id) returns ReadOnlyBracedUnion {
        return "x";
    }

    resource function get res11(string id) returns ReadOnlyRec {
        return {
            id: "xx",
            name: "Kitty"
        };
    }

    resource function get res12()
            returns http:InternalServerError & readonly|http:Created & readonly|http:Conflict & readonly {
        return http:CREATED;
    }

    resource function get res13() returns
            readonly & (string|int) |
            readonly & string|int |
            readonly & map<json> |
            string |
            readonly & Pet |
            readonly & record {|
                readonly string id;
                string name;
            |} {
        return "Hello World";
    }
}
