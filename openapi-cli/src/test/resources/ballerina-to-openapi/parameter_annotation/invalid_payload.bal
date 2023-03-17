import ballerina/http;

listener http:Listener ep0 = new (443, config = {host: "petstore3.swagger.io"});

type Student record {
    string Name;
};

service /payloadV on ep0 {
    resource function post student12(Student[] students, map<json> q) returns json {
       string name = students[0].Name;
       return {Name: name};
    }
}
