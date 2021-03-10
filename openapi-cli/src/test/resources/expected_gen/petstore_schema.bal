type  Pet record  { 
    int  id;
    string  name;
    string  tag?;
    string  'type?;
};

type  Dog record  { 
    *Pet;
    boolean  bark?;
};

type  Pets record  { 
    Pet[]  petslist;
};

type  Error record  { 
    int  code;
    string  message;
};
