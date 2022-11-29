import ballerina/http;

listener http:Listener ep0 = new (443, config = {host: "api.example.com"});

service / on ep0 {
    resource function get store/inventory() returns Inline_response_200 {
    }
    resource function get store/inventory02() returns record {|Inline_response_map200...;|}|string {
    }
    resource function get store/inventory03() returns record {|record {|record {|string name?; string place?;|}...;|}...;|} {
    }
    resource function get store/inventory04() returns record {|User...;|} {
    }
    resource function get store/inventory05() returns BadRequestRecordUser {
    }
}
