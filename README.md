# Sociable Weaver

The [Sociable Weaver webpage](https://sociable-weaver.github.io/web/) expects this application to be running on the
users' computer. The webpage communicates with the application using port `8077`. This application requires Java 17 or
newer to run.

When a new tag is pushed to _origin_ (the [GihHub repository](https://github.com/sociable-weaver/app-java-boot)), a
pipeline is triggered when the application is built, tested and
the [released](https://github.com/sociable-weaver/app-java-boot/releases). The Sociable
Weaver [home page](https://github.com/sociable-weaver/web-vue3/blob/main/src/components/App.vue#L13-L17) has a link from
where the user can download this application. This link is version specific and needs to be updated, after the new
version of this application is released.

## Useful commands

Gradle

- Run all tests

  ```shell
  $ ./gradlew check
  ```

- Run the application locally, ideal for development

  ```shell
  $ ALLOWED_ORIGIN="http://localhost:8080" ./gradlew bootRun
  ```

  Note that by default the app accepts requests from: `https://sociable-weaver.github.io`.

- Build the application for production

  ```shell
  $ ./gradlew build
  ```

Docker

- Create the docker local image

  ```shell
  $ docker build . -t app:local
  ```

- Run the docker local image

  ```shell
  $ docker run \
      --name 'app-local' \
      --publish 8077:8077 \
      --volume '/Users/albertattard/Projects/albertattard:/opt/repositories' \
      --volume '/Users/albertattard/Projects/workspace:/opt/workspace' \
      --env ALLOWED_ORIGIN='http://localhost:8080' \
      --rm \
      app:local
  ```

- Connect to the docker container

  ```shell
  $ docker exec -it 'app-local' /bin/bash
  ```

Sonarqube

- Start the local sonarqube server

  ```shell
  $ docker compose up -d
  ```

  Two docker images are started, [Sonarqube Community](https://hub.docker.com/_/sonarqube)
  and [PostgreSQL](https://hub.docker.com/_/postgres), and each uses several volumes, to preserve state between runs.

- List running docker containers

  ```shell
  $ docker ps
  ```

  This should list the two docker containers

  ```shell
  CONTAINER ID   IMAGE                       COMMAND                  CREATED          STATUS          PORTS                                            NAMES
  53469dcd536a   postgres:9.6.24             "docker-entrypoint.s…"   26 seconds ago   Up 24 seconds   5432/tcp                                         sociable-weaver-app-java-boot-db-1
  79dec465d1b9   sonarqube:9.2.1-community   "/opt/sonarqube/bin/…"   26 seconds ago   Up 24 seconds   0.0.0.0:9000->9000/tcp, 0.0.0.0:9092->9092/tcp   sociable-weaver-app-java-boot-sonarqube-1
  ```

  Note that there may be many more docker containers running on your machine.

- List the volumes

  ```shell
  $ docker volume ls
  ```

  This docker compose creates the following six volumes.

  ```shell
  DRIVER    VOLUME NAME
  local     sociable-weaver-app-java-boot_postgresql_data
  local     sociable-weaver-app-java-boot_sonarqube_bundled-plugins
  local     sociable-weaver-app-java-boot_sonarqube_conf
  local     sociable-weaver-app-java-boot_sonarqube_data
  local     sociable-weaver-app-java-boot_sonarqube_db
  local     sociable-weaver-app-java-boot_sonarqube_extensions
  ```

  Note that there may be many more docker volumes on your machine.

- Create security token

  Open [http://localhost:9000](http://localhost:9000) and log in with, `admin`/`admin`. You will be prompted to change
  the password. Generate a [security token](http://localhost:9000/account/security/). Store the security token as this
  will be used to run the `sonarqube` Gradle task locally. You will **not** be able to access the security token again.

- Configure mutation testing (pitest)

  **Was not able to get this working and I recommend you skip this part until this is sorted up!!**

  Open the [marketplace](http://localhost:9000/admin/marketplace?search=pitest) and search for `pitest`. Click install
  to install this plugin. Restart the sonarqube server as prompted.

- Run the tests and publish their results

  You need a [security token](http://localhost:9000/account/security/) to run this command, which was generated in a
  previous step.

  ```shell
  $ ./gradlew \
     -Dsonar.login=b2e3b86de07f67403a4461ae4478040f46f8a99a \
     -Dsonar.host.url=http://localhost:9000 \
      sonarqube
  ```

- Review results

  Review the [results](http://localhost:9000/component_measures?id=sociable-weaver-app-java-boot)

- Stop the local sonarqube server

  ```shell
  $ docker compose down
  ```

- Delete the volumes

  ```shell
  $ docker volume rm sociable-weaver-app-java-boot_postgresql_data
  $ docker volume rm sociable-weaver-app-java-boot_sonarqube_bundled-plugins
  $ docker volume rm sociable-weaver-app-java-boot_sonarqube_conf
  $ docker volume rm sociable-weaver-app-java-boot_sonarqube_data
  $ docker volume rm sociable-weaver-app-java-boot_sonarqube_db
  $ docker volume rm sociable-weaver-app-java-boot_sonarqube_extensions
  ```

  Alternatively, delete all volumes, not just those that belong to this docker compose file.

  ```shell
  $ docker volume rm $(docker volume ls -q)
  ```

Git

- Commit changes

  ```shell
  $ git commit -m "Add tag example"
  ```

- Push changes to _origin_

  ```shell
  $ git push
  ```

- List all tags

  ```shell
  $ git tag
  ```

- Tag the current commit and push to _origin_

  ```shell
  $ git tag v0.1
  $ git push origin v0.1
  ```

  This will trigger the build pipeline and release the application. Note that the tag needs to be unique.
