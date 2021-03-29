
service hello on new http:Listener(9090) {
    resource function get student/[string userId] () {
    }
    //resource function get student/{int sid}/course/{int cid} () {
    //}
}
