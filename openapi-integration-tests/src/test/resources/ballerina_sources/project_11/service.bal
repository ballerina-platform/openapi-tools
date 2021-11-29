import ballerina/graphql;

service graphql:Service /query on new graphql:Listener(8080) {
   resource function get name() returns string {
       return "Jack";
   }
}
