import ballerina/openapi;

@openapi:ClientInfo {
    tags:["abc", "ads"],
    operations: ["o1"],
    nullable: false
}
client "https://raw.githubusercontent.com/ballerina-platform/openapi-connectors/main/openapi/openweathermap/openapi.yaml" as foo;

public function main() {
    @openapi:ClientInfo {
        tags:["abc", "ads"],
        operations: ["o1"],
        nullable: false
    }
    client "https://raw.githubusercontent.com/ballerina-platform/openapi-connectors/main/openapi/openweathermap/openapi.yaml" as bar;
    bar:client y;
    foo:client x;
}
