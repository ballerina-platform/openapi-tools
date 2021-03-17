type  Pet record  {
    int  id;
    string  name;
    string  tag?;
};

type  Dog record  {
    Pet  pets?;
    boolean  bark;
};