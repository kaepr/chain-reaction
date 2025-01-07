create table users (
  id integer primary key autoincrement,
  username text not null unique,
  password text not null,
  created_at datetime default current_timestamp
);

create table session_store (
  session_id varchar not null primary key,
  idle_timeout integer,
  absolute_timeout integer,
  value BLOB
);
