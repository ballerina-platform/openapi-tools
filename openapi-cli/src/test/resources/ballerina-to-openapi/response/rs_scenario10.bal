import ballerina/http;

listener http:Listener helloEp = new (9090);
type  Pet record  {
    int  id;
    string  name;
    string  tag?;
    string  'type?;
};

service /payloadV on helloEp {
    resource function post hi01() returns Pet[] {
        Pet pet = {
            id: 10,
            name: "tommy"
        };
        Pet[] pr = [pet];
        return  pr;
    }
    resource function post hi() returns string[] {
        string[] st = ["test"];
        return st;
    }
}
