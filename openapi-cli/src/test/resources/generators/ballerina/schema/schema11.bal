type  Pet record  {
    int  id;
    string  name;
    Tag  tag?;
};

type  Dog record  {
    Pet[]  pets?;
    boolean  bark;
};

type  Tag record  {
    int  id?;
    string  tagType;
};
