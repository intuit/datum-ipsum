create table if not exists names (
name string,
type string
)
row format delimited fields terminated by ',';

load data local inpath 'names.csv' overwrite into table names;
