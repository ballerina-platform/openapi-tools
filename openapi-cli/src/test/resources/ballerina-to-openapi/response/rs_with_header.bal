import ballerina/http;

public type RateLimitHeaders record {|
    string x\-rate\-limit\-id;
    int x\-rate\-limit\-remaining;
    string[] x\-rate\-limit\-types;
    boolean x\-rate\-limit\-enable;
|};

public type OkWithRateLmits record {|
    *http:Ok;
    RateLimitHeaders headers;
    string body;
|};

service /payloadV on new http:Listener(9090) {
    resource function get status() returns OkWithRateLmits {
        OkWithRateLmits okWithRateLmits = {
            headers: {
                x\-rate\-limit\-id: "1xed",
                x\-rate\-limit\-remaining: 3,
                x\-rate\-limit\-types: ["sliver", "gold"],
                x\-rate\-limit\-enable: true
            },
            body: "full"
        };
        return okWithRateLmits;
    }
}
