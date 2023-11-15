import ballerina/http;

listener http:Listener helloEp = new (9090);

type Record record {|
    string name;
    string city;
|};

type Header record {|
    string header1;
    int[] header2;
    float[] header3;
    boolean header4;
|};

service /payloadV on helloEp {

    resource function get header(@http:Header Header h, @http:Header string h0 = "", @http:Header string h1 = "\"John\"",
            @http:Header string[] h2 = [], @http:Header string[] h3 = ["one", "two", "three"],
            @http:Header int[] h4 = [1, 2, 3], @http:Header float[] h5 = [1, 2.3, 4.56],
            @http:Header Record h6 = {name: "John", city: "London"},
            @http:Header Header h7= {header1: "header1", header2: [1,2,3], header4: false, header3: []}) returns string {
        return "new";
    }
}
