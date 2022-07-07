import ballerina/http;

listener http:Listener helloEp = new (9090);

type TagType record {
    int id;
    string[] category;
};

type Tag record {
    int id;
    string name;
    TagType[] tagType;
};

type Pet record {
    int id;
    string name;
};

@http:ServiceConfig {
         mediaTypeSubtypePrefix : "vnd.wso2.sales"
 }

service /payloadV on helloEp {
    resource function post pets(@http:Payload {} Pet payload) returns string {
        return "pet";
    }

    resource function get pets() returns json {
        json ok = "Apple";
        return ok;
    }

    resource function get pet() returns xml {
        xml x1 = xml `<book>The Lost World</book>`;
        return x1;
    }

    resource function post hi() returns string[] {
        string[] st = ["test"];
        return st;
    }
}
