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

Sonarqube

- Start the local sonarqube server

  ```shell
  $ docker compose up -d
  ```

  Open [http://localhost:9000](http://localhost:9000) and log in with, `admin`/`admin` and generate
  a [security token](http://localhost:9000/account/security/). This will be used to run the `sonarqube` Gradle task
  locally.

  Two docker images are started, [Sonarqube Community](https://hub.docker.com/_/sonarqube)
  and [PostgreSQL](https://hub.docker.com/_/postgres), and each uses several volumes, to preserve state between runs.

- Run locally

  You need a [security token](http://localhost:9000/account/security/) to run this command.

  ```shell
  $ ./gradlew \
     -Dsonar.login=86df1d07a0fe9c13b16cd0cf0330dc22ee8e90ce \
     -Dsonar.host.url=http://localhost:9000 \
      sonarqube
  ```

- Start the local sonarqube server

  ```shell
  $ docker compose down
  ```

- List the volumes

  ```shell
  $ docker volume ls
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
