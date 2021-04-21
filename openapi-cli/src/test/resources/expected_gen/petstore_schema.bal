public type Pet record  {
    int  id;
    string  name;
    string  tag?;
    string  'type?;
};

public type Dog record  {
    *Pet;
    boolean  bark?;
};

public type Pets record  {
    Pet[]  petslist;
};

public type Error record  {
    int  code;
    string  message;
};
