# {{raw-name}}

{{description}}

## Usage

FIXME: write usage documentation!

Invoke a library API function from the command-line:

    $ clojure -X {{top/ns}}.{{main/ns}}/foo :a 1 :b '"two"'
    {:a 1, :b "two"} "Hello, World!"

Run the project's tests (they'll fail until you edit them):

    $ bb test     # run Clojure tests
    $ bb test:bb  # run Babashka tests
    $ bb test:all # run both Clojure and Babashka tests

Run the project's CI pipeline and build a JAR (this will fail until you edit the tests to pass):

    $ bb ci

This will produce an updated `pom.xml` file with synchronized dependencies inside the `META-INF`
directory inside `target/classes` and the JAR in `target`. You can update the version (and SCM tag)
information in generated `pom.xml` by updating `build.clj`.

Install it locally (requires the `ci` task be run first):

    $ bb install

Run the projects's CI pipeline and deploy it to Clojars --
needs `CLOJARS_USERNAME` and `CLOJARS_PASSWORD` environment
variables:

    $ bb ci:deploy

Your library will be deployed to {{group/id}}/{{artifact/id}} on clojars.org by default.

## License

Copyright © {{now/year}} {{developer}}

Distributed under the
[{{license/name}}]({{license/url}})
