import ballerina/io;
import ballerina/openapi;

@openapi:ClientConfig {
    operations: ["getUSCountiesStatus"],
    nullable: true
}
client "https://raw.githubusercontent.com/ballerina-platform/openapi-connectors/main/openapi/covid19/openapi.yaml" as bar;

public function main() returns error? {
    bar:client countryClient = check new ();
    bar:CovidJHUCounties country = check countryClient->/v3/covid\-19/jhucsse/counties.get;
    if country is bar:CovidJHUCounty[] {
        io:println(country[0]);
    }
}
