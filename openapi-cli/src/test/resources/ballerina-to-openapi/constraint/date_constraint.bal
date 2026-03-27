import ballerina/constraint;
import ballerina/http;
import ballerina/time;

public type UserProfile record {
    string name;

    @constraint:Date {
        option: constraint:PAST,
        message: "Date of birth must be in the past"
    }
    time:Date dateOfBirth;
};

listener http:Listener ep0 = new (8080);

service /api on ep0 {
    resource function post users(@http:Payload UserProfile payload) returns http:Created {
        return http:CREATED;
    }
}
