 import ballerina/http;

 listener http:Listener helloEp = new (9090);

 service /hello/foo/bar on helloEp {
     resource function get hi/[int abc]() {

     }
     resource function post hi(int id) {

     }
 }

 service /hello02/bar/baz on helloEp {
     //resource function get hi/[int abc]() {
     //
     //}
     resource function post hi() {

     }
 }
