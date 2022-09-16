import ballerina/openapi;

@openapi:ClientInfo {
    tags:["abc", "ads"],
    operations: ["o1"]
    nullable: false,
    isResource: false
    withTests: true,
    license: "ttt.txt"
}
client "https://raw.githubusercontent.com/ballerina-platform/openapi-connectors/main/openapi/openweathermap/openapi.yaml" as bar;
