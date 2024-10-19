import openapi_bal_ext.module as mod;

import ballerina/http;
import ballerina/time;

type Subject record {|
    string name;
    int credits;
    time:Date examDate;
|};

public type Student record {|
    *mod:BasicStudent;
    Subject[] subjects;
|};

service /api/v1 on new http:Listener(9090) {

    resource function get students() returns Student[] {
        return [
            {
                name: "John",
                age: 25,
                subjects: [
                    {name: "Math", credits: 4, examDate: {day: 10, month: 10, year: 2023}},
                    {name: "Science", credits: 3, examDate: {day: 15, month: 10, year: 2023}}
                ]
            }
        ];
    }
}

