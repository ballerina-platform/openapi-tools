import 'service.representations as rep;

service /payloadV on rep:ep01, rep:ep02 {
    resource function get pets() returns string {
            return "done";
    }
}
