public type OpenapiClient record {|
    string[] tags?;
    string[] operation?;
    boolean nullable?;
    boolean resource?;
    boolean withTest?;
|};

public annotation OpenapiClient openapiClient on var;

client "http://www.example.com/apis/one.yaml" as foo;

public function main() {
    client "http://www.example.com/apis/one.yaml" as bar;
    foo:client x;
    bar:client y;
}
