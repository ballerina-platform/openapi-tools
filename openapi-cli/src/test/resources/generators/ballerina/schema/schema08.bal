
type  Pet record  {
    int  id;
    string  name;
    string  tag?;
    string  'type?;
};

type  Pets record  {
    Pet[]  petslist;
};
