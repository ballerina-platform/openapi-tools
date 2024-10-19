import ballerina/http;
import ballerina/time;

type AppointmentTable table<Appointment> key(id);

public type Appointment record {|
    readonly string id;
    string description;
    string doctorId;
    time:Date date?;
|};

public type Pet record {|
    string name;
    readonly string tag;
    decimal age;
    AppointmentTable appointments?;
|};

table<Pet> key(tag) pets = table [
    {name: "Tommy", tag: "dog", age: 2.5},
    {
        name: "Lucy",
        tag: "cat",
        age: 1.5,
        appointments: table [
            {id: "1", description: "Vaccination", doctorId: "1"},
            {id: "2", description: "Checkup", doctorId: "2"}
        ]
    }
];

service /payloadV on new http:Listener(9090) {

    resource function get pets() returns table<Pet> {
        return pets;
    }

    resource function get pets/[string tag]() returns Pet {
        return pets.get(tag);
    }

    resource function get pets/[string tag]/appointments() returns AppointmentTable {
        return pets.get(tag).appointments ?: table [];
    }
}
