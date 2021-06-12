#
#+id-Field Description
#+name-Field Description
#+tag-Field Description
public type Pet record {
    int id;
    string name;
    string tag?;
};
#
#+pets-Field Description
#+bark-Field Description
public type Dog record {
    Pet pets?;
    boolean bark;
};
