import ballerina/http;
import 'service.types;

service /payloadV on new http:Listener(8080) {
    private map<types:Product> products = {};
    # List all products
    # + return - List of products
    resource function get .() returns types:Product[] {
        return self.products.toArray();
    }
}
