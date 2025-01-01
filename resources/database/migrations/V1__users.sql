create table users (
  id integer primary key autoincrement,
  username text not null unique,
  password text not null,
  created_at integer default current_timestamp,
  updated_at integer default current_timestamp
);
