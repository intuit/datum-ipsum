create table if not exists simple (
example string,
row int
)
row format delimited fields terminated by ',';

load data local inpath 'simple.csv' overwrite into table simple;
