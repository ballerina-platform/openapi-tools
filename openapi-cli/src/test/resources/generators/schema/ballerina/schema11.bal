#
#+id-Field Description
#+name-Field Description
#+tag-Field Description
#+'type-Field Description
#+typeId-Field Description
#+tagType-Field Description
public type Pet record {
    int id;
    string name;
    string tag?;
    record { string typeId?; string tagType?;} 'type?;
};
