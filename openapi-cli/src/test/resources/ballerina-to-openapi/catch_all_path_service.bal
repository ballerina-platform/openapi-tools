import ballerina/http;

public type Category record {
    int id?;
    string name?;
};

public type Tag record {
    int id?;
    string name?;
};

public type Pet record {
    int id?;
    string name;
    Category category?;
    string[] photoUrls;
    Tag[] tags?;
    string status?;
};

listener http:Listener ep0 = new (443, config = {host: "petstore3.swagger.io"});

service /payloadV on ep0 {
    resource function get '\*(int petId) returns Pet|error {
        return {id: 1, name: "doggie", photoUrls: ["http://foo.bar.com/1", "http://foo.bar.com/2"]};
    }
}
