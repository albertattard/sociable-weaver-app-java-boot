# Sociable Weaver

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

Git

- Commit changes

  ```shell
  $ git commit -m "Add tag example"
  ```

- Push changes to _origin_

  ```shell
  $ git push
  ```

- Tag the current commit and push to _origin_

  ```shell
  $ git tag v0.1
  $ git push origin v0.1
  ```

  This will trigger the build pipeline and release the application.  Note that the tag needs to be unique.
