import ballerina/http;

listener http:Listener ep0 = new (443, config = {host: "petstore3.swagger.io"});

type Student record {
    string Name;
};

service /payloadV on ep0 {
    resource function post student1(@http:Payload Student student, map<json> q) returns json {
        return {Name: student.Name};
    }

    resource function post student2(@http:Payload string p, string q) returns json {
        return {Name: p};
    }

        resource function post student3(@http:Payload string? p, string q) returns json {
            return {Name: p ?: q};
        }
}
