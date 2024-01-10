client "https://raw.githubusercontent.com/ballerina-platform/graphql-tools/main/graphql-cli/src/test/resources/specs/graphql-config-with-basic-auth-client-config.yaml" as foo;

public function main() {
    foo:client x;
}
