import ballerina/openapi;
import ballerina/http;

public type Link record {
    string rel?;
    string href;
    string[] types?;
    string[] methods?;
};

public type Location record {|
    map<Link> _links;
    map<string> name;
    map<int> id;
    map<float> addressCode;
    map<json> item?;
    map<string[]> mapArray?;
    map<map<json>> mapMap?;
    map<string>[] arrayItemMap?;
|};

@openapi:ServiceInfo {
    embed: true
}
service /payloadV on new http:Listener(9090) {
    resource function get locations() returns Location {
        return {
            name: {"name":"Alps"},
            id: {"id": 10},
            addressCode: { "code" :29384.0},
            _links: {
                room: {
                    href: "/snowpeak/locations/{id}/rooms",
                    methods: ["GET"]
                }
            }
        };
    }
}
