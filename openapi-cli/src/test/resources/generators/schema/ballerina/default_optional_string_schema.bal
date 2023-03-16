public type Pet record {
    int id;
    string name;
    string tagName = "TagName";
    string? 'type = "<|endoftext|>";
    string|string[]? prompt = "<|endoftext|>";
    int|string anyOfField = "<|endoftext|>";
};
