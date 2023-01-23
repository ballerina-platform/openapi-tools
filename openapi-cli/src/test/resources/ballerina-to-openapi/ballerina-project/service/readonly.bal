import ballerina/http;
import 'service.representations as ds;

service /payloadV on new http:Listener(9090) {

    # A list of all `Lift`s
    # + return - the lifts
    resource function get lifts(string? status = ()) returns ds:LiftRecord[] {
        ds:LiftRecord[] lifts;
        if status !is () {
            lifts = from var lift in ds:liftTable
                where lift.status == status
                select lift;
        } else {
            lifts = ds:liftTable.toArray();
        }
        return lifts;
    }

    # Returns a `Lift` by `id` (id: "panorama")
    # + return - the lift
    resource function get lift(string id) returns ds:LiftRecord? {
        ds:LiftRecord[] lifts = from var lift in ds:liftTable
            where lift.id == id
            select lift;
        if lifts.length() > 0 {
            return lifts[0];
        }
        return;
    }
}
