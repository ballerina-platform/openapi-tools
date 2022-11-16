import ballerina/openapi;

@openapi:ClientConfig {
    tags:["abc", "ads"],
    operations: ["o1"],
    nullable: false
}
client "https://raw.githubusercontent.com/ballerina-platform/openapi-connectors/main/openapi/openweathermap/openapi.yaml" as foo;

@openapi:ClientConfig {
    tags:["abc", "ads"],
    operations: ["o1"],
    nullable: false
}
client "https://raw.githubusercontent.com/ballerina-platform/openapi-connectors/main/openapi/openweathermap/openapi.yaml" as bar;

public function main() {
    bar:client y;
    foo:client x;
}
