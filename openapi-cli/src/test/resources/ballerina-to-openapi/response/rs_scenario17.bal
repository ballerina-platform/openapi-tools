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
    resource function post pets(@http:Payload {} Pet payload) returns Pet {
        Pet pet = {
            id: 10,
            name: "tommy"
            };
        return pet;
    }
}
