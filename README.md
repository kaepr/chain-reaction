# Chain Reaction 

Chain reaction game implemented as a multiplayer web app.

Deployed Link: [chain-reaction-app.fly.dev](https://chain-reaction-app.fly.dev)

Please note that the first load could take a while (around 1 min) to load.


https://github.com/user-attachments/assets/ca69469f-b8f8-4ea2-9a7e-fbbb085218a9


## Run Locally

Make sure [Clojure](https://clojure.org) is installed.

Refer `resources/config.edn` for all environment variables and configuration.

The application uses sqlite for storing user data. 
Create a file locally at project root called `local.db`. 
(Refer to config file above for using a different name, or switching to in memory sqlite `:memory:`).

``` sh
touch local.db
```

Run the application locally using.

``` sh
clj -M:repl
```

Start the web server locally by evaluating `(go)` in the REPL. 

```clojure
(go) ; start's the server 
```

Refer `dev/user.clj` for other commands for starting / halting / resetting the server.

For building CSS, make sure [tailwind cli](https://tailwindcss.com/docs/installation) is installed.
Evaluate `(start-css-watch)` in `dev/css.clj`.
It starts a tailwindcss process which watches the `src` directories and rebuilds css if necessary.

# Build

```shell
clj -T:build uber
```

Creates a standlone uberjar `/target` folder.

# Deployment 

Refer `Dockerfile` for creating the image. I used [fly.io](fly.io) for deploying the docker image.

Used [Practicalli | Multi-Stage Dockerfile for Clojure](https://practical.li/engineering-playbook/continuous-integration/docker/clojure-multi-stage-dockerfile/) as reference for creating the Dockerfile.
