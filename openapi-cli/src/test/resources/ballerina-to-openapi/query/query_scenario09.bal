import ballerina/http;

listener http:Listener helloEp = new (9090);

type Record record {|
    string name;
    map<string> address;
|};

service /payloadV on helloEp {

    resource function get query(string q0 = "", string q1 = string `"John"`,
            string[] q2 = [], string[] q3 = ["one", "two", "three"], int[] q4 = [1, 2, 3],
            float[] q5 = [1, 2.3, 4.56], map<string> q6 = {"name": "John", "city": "London"},
            map<string>[] q7 = [{"name": "John", age: "25"}, {name: "David", age: "30"}],
            Record q8 = {name: "John", address: {number: "14/7", streetName: "2nd cross street", city: "London"}}, map<float> q9 = {},
            boolean[] q10 = [true, false, true]) returns string {
        return "new";
    }
}
