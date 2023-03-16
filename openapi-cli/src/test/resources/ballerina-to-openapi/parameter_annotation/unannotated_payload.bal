import ballerina/http;

listener http:Listener ep0 = new (443, config = {host: "petstore3.swagger.io"});

type Student record {
    string Name;
};

service /payloadV on ep0 {

    resource function post student1(Student student) returns json {
        string name = student.Name;
        return {Name: name};
    }

    resource function post student2(Student student, string id) returns json {
        string name = student.Name;
        return {Name: name, id: id};
    }

    resource function post student3(Student[] students) returns json {
        string name = students[0].Name;
        return {Name: name};
    }
}
