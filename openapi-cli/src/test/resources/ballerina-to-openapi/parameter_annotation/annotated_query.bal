import ballerina/http;

listener http:Listener ep0 = new (443, config = {host: "petstore3.swagger.io"});

public const RESOURCE_KIND_SERVICE = "Service";
public const RESOURCE_KIND_POD = "Pod";

public type ResourceKind RESOURCE_KIND_SERVICE|RESOURCE_KIND_POD;

type Student record {
    string Name;
};

enum Status {
    ACTIVE,
    INACTIVE
};

service /payloadV on ep0 {

    resource function post student8(@http:Query map<json> student) returns json {
        return {Name: "john"};
    }

    resource function post student9(@http:Query map<json> students = {"Name" : "John"}) returns Student {
            return {Name: "john"};
    }

    resource function post student10(@http:Query Status status) returns Student {
            return {Name: "john", "Status": status};
    }

    resource function post student11(@http:Query Status status = "ACTIVE") returns json {
            return {Name: "john", Status: status};
    }

    resource function post student12(@http:Query string status = RESOURCE_KIND_SERVICE) returns json {
            return {Name: "john", Status: status};
    }

    resource function post student13(@http:Query ResourceKind kind = RESOURCE_KIND_SERVICE) returns json {
            return {Name: "john"};
    }
}
