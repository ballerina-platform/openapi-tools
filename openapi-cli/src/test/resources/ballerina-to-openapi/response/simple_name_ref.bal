import ballerina/http;

public type Pet record {
    string name;
};

public type ReturnValueStr string;

public type ReturnValueStrNil string?;

public type ReturnValueStrArray string[];

public type ReturnValueMapJson map<json>;

public type ReturnValueMapRec map<Pet>;

public type ReturnValueMapString map<string>;

public type ReturnValueJson json;

public type ReturnValueXML xml;

public type ReturnValueError error;

public type ReturnValuePrimitiveUnion string|int;

public type Dog record {
    string breed;
    boolean isBark;
};

public type ReturnValueRecUnion Pet|Dog;

public type ErrorRes http:BadRequest;

// public type ReturnValueStrNilArray string?[]?;

// public type ReturnValuePrimitiveUnionArr (string|int)[];

// public type ReturnValueNilPrimitiveUnionArr (string|int?)[]?;

// public type ReturnValueNilPrimitiveUnionArrAlt (string?|int?)[]?; // equivalent to the above case

service /payloadV on new http:Listener(9090) {

    resource function get lift01(string id) returns ReturnValueStr {
        return "Value";
    }

    resource function get lift02(string id) returns ReturnValueStrNil {
        return ();
    }

    resource function get lift03(string id) returns ReturnValueStrArray {
        string[] values = ["val1", "val2"];
        return values;
    }

    resource function get lift05(string id) returns ReturnValueMapJson {
        return {"map": {}};
    }

    resource function get lift06(string id) returns ReturnValueJson {
        return {
            "name": "test-user"
        };
    }

    resource function get lift07(string id) returns ReturnValueMapRec {
        return {"map": {
            name: "Kitty"
        }};
    }

    resource function get lift08(string id) returns ReturnValueMapString {
        return {"kitty": "cat"};
    }

    resource function get lift09(string id) returns ReturnValueXML {
        ReturnValueXML x1 = xml `<book>The Lost World</book>`;
        return x1;
    }

    // need to discuss
    resource function get lift10(string id) returns ReturnValueError {
        return error("Value");
    }

    resource function get lift11(string id) returns ReturnValuePrimitiveUnion {
        return "value";
    }

    resource function get lift12(string id) returns ReturnValueRecUnion {
        return {
            name: "Kitty"
        };
    }

    resource function get res1(string id) returns ErrorRes {
        return {
            mediaType: "json"
        };
    }

    // // not working after new implementation //not supported in Ballerina
    // resource function get lift13(string id) returns ReturnValueStrNilArray {
    //     string?[] values = ["val1", ""];
    //     return values;
    // }

    // // not working //not supported in Ballerina
    // resource function get lift14(string id) returns ReturnValuePrimitiveUnionArr {
    //     (string|int)[] values = ["val", 1];
    //     return values;
    // }

    // // not working //not supported in Ballerina
    // resource function get lift15(string id) returns ReturnValueNilPrimitiveUnionArr {
    //     (string|int?)[]? values = ["val", 1, ()];
    //     return values;
    // }

    // // not working //not supported in Ballerina
    // resource function get lift16(string id) returns ReturnValueNilPrimitiveUnionArrAlt {
    //     (string|int?)[]? values = ["val", 1, ()];
    //     return values;
    // }
}
