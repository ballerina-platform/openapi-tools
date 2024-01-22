import ballerina/http;

listener http:Listener helloEp = new (9090);

type Record record {|
    string name;
    string city;
|};

type Header1 record {|
    string header11;
    int[] header12;
    float[] header13;
    boolean header14;
|};

type Header2 record {|
    string header21;
    int[] header22;
    float[] header23;
    boolean header24;
|};

service /payloadV on helloEp {

    resource function get header(@http:Header Header1 h, @http:Header string h0 = "", @http:Header string h1 = "\"John\"",
            @http:Header string[] h2 = [], @http:Header string[] h3 = ["one", "two", "three"],
            @http:Header int[] h4 = [1, 2, 3], @http:Header float[] h5 = [1, 2.3, 4.56],
            @http:Header Record h6 = {name: "John", city: "London"},
            @http:Header Header2 h7= {header21: "header1", header22: [1,2,3], header24: false, header23: []}) returns string {
        return "new";
    }
}
