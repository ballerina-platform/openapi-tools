#
#+id-Field Description
#+name-Field Description
#+tag-Field Description
#+'type-Field Description
public type Pet record {
    int id;
    string name;
    string tag?;
    string 'type?;
};
#
#+bark-Field Description
public type Dog record {
    *Pet;
    boolean bark?;
};
