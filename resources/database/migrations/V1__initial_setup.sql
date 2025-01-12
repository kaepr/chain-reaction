-- create table users (
--   id integer primary key autoincrement,
--   username text not null unique,
--   password text not null,
--   created_at datetime default current_timestamp
-- );

-- create table session_store (
--   session_id varchar not null primary key,
--   idle_timeout integer,
--   absolute_timeout integer,
--   value BLOB
-- );

CREATE TABLE users (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  username TEXT NOT NULL UNIQUE,
  password TEXT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE session_store (
  session_id VARCHAR NOT NULL PRIMARY KEY,
  idle_timeout INTEGER,
  absolute_timeout INTEGER,
  value BLOB
);

CREATE TABLE matches (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  player_1 INTEGER NOT NULL,
  player_2 INTEGER NOT NULL,
  winner INTEGER NOT NULL,
  match_result TEXT CHECK (match_result IN ('finished', 'dnf')) NOT NULL,

  FOREIGN KEY (player_1) REFERENCES users(id),
  FOREIGN KEY (player_2) REFERENCES users(id),
  FOREIGN KEY (winner) REFERENCES users(id)
);
