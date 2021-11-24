# Sociable Weaver

## Useful commands

Gradle

- Run all tests

  ```shell
  $ ./gradlew check
  ```

- Run the application locally, ideal for development

  ```shell
  $ ./gradlew bootRun
  ```

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
