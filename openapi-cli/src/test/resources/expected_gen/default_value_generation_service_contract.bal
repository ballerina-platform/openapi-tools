// AUTO-GENERATED FILE.
// This file is auto-generated by the Ballerina OpenAPI tool.

import ballerina/http;

@http:ServiceConfig {basePath: "/payloadV"}
type OASServiceType service object {
    *http:ServiceContract;
    resource function get albums/[string id](string q1 = "query1", int q2 = -1, @http:Header string X\-HEADER = "header1") returns Album;
};

public type Album record {|
    int iid = -1;
    string title;
    string author;
    Genre genre = {"name":"Unknown","description":"Unknown","iid":-1};
    string[] tags = ["tag1","tag2"];
    (string|int)[] ratings = ["unrated",5,4];
    decimal price = 100.55;
    boolean available = true;
|};

public type Genre record {|
    string name = "Unknown";
    string|string[] description = ["Unknown","Unknown"];
    int iid;
|};
