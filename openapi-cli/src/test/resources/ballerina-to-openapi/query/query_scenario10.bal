import ballerina/http;

listener http:Listener helloEp = new (9090);

type Record record {|
    string name;
    map<string> address;
|};

const query1 = "query1";

service /payloadV on helloEp {

    resource function get query(
            @http:Query{name: "query0"} string q0 = "",
            @http:Query{name: query1} string q1 = string `"John"`,
            @http:Query{name: "query2"} string[] q2 = [],
            @http:Query{name: "query3"} string[] q3 = ["one", "two", "three"],
            @http:Query{name: "query4"}  int[] q4 = [1, 2, 3],
            @http:Query{name: "query5"} float[] q5 = [1, 2.3, 4.56],
            @http:Query{name: "query6"} map<string> q6 = {"name": "John", "city": "London"},
            @http:Query{name: "query7"} map<string>[] q7 = [{"name": "John", age: "25"},
                {name: "David", age: "30"}],
            @http:Query{name: "query8" } Record q8 = {name: "John", address: {number: "14/7",
                streetName: "2nd cross street", city: "London"}},
            @http:Query{name: "query9"} map<float> q9 = {},
            @http:Query{name: "query10"} boolean[] q10 = [true, false, true]
        ) returns string {
        return "new";
    }
}
