public type Holder record {
    string[]? tags?;
    Labels? labels?;
    Tags? names?;
};

public type Labels record {|
    string...;
|};

public type Tags string[]?;
