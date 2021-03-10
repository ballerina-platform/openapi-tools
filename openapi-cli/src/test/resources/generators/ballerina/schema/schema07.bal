type  Pet record  {
    int  id;
    string  name;
    string  tag?;
    Tag  'type?;
};

type  Dog record  {
    Pet[]  pets?;
    boolean  bark;
};

type  Tag record  {
    int  id?;
    string  tagType?;
};