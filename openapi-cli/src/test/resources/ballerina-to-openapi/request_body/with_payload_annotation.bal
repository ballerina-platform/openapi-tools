import ballerina/http;

listener http:Listener helloEp = new (9090);

type Reservation record {|
    string roomId;
    int count;
    string startDate;
    string endDate;
|};

service /payloadV on helloEp {
    resource function post reservation(@http:Payload Reservation reservation) returns http:Ok {
        http:Ok ok = {body: ()};
        return ok;
    }
}
