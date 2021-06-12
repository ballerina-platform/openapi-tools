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
#+code-Field Description
#+message-Field Description
public type Error record {
    int code;
    string message;
};
