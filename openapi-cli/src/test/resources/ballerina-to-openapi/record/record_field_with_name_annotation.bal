import ballerina/data.jsondata;
import ballerina/http;

@http:ServiceConfig {basePath: "/payloadV"}
type OASServiceType service object {
    *http:ServiceContract;
    resource function get albums(string[] artists = [], @http:Header {name: "X-API-VERSION"} string? xAPIVERSION = "v1") returns Album[];
    resource function post albums(Album payload) returns Album;
    resource function get albums/[string id]() returns Album;
};

const titleField = "_title";

public type Album record {|
    string artist;
    @jsondata:Name {value: "_id"}
    string id;
    @jsondata:Name {value: titleField}
    string title;
|};
