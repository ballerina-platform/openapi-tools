#
#+id-Field Description
#+name-Field Description
#+tag-Field Description
#+'type-Field Description
public type Pet record {
    int id;
    string name;
    string tag?;
    Tag 'type?;
};

#
#+pets-Field Description
#+bark-Field Description
public type Dog record {
    Pet[] pets?;
    boolean bark;
};

#
#+id-Field Description
#+tagType-Field Description
public type Tag record {
    int id?;
    string tagType?;
};
