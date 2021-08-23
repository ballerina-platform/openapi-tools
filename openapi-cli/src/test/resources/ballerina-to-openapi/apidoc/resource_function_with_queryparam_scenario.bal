import ballerina/http;

service /payloadV on new http:Listener(9090) {

    # Reperesents Snowpeak room collection resource
    #
    # + id - Unique identification of location
    # + startDate - Start date in format yyyy-mm-dd
    # + endDate - End date in format yyyy-mm-dd
    resource function get locations/[string id]/rooms(string startDate, string endDate) {
    }
}
