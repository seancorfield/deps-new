# deps-new

The next generation of `clj-new`. Work-in-progress!

Intended to be installed as a "tool" (Clojure CLI 1.10.3.933 or later).

```bash
clojure -Ttools install com.github.seancorfield/deps-new '{:sha "..."}' :as deps-new

clojure -Tdeps-new app :name myusername/mynewapp
```

Creates a directory `mynewapp` containing a new application project, with `myusername` as the "top" namespace
and `mynewapp` as the main project namespace:

```clojure
;; mynewapp/src/myusername/mynewapp.clj
(ns myusername.mynewapp
  (:gen-class))

(defn greet
  "Callable entry point to the application."
  [data]
  (println (str "Hello, " (or (:name data) "World") "!")))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (greet {:name (first args)}))
```

Currently there's only that one built-in template (`app`).

More general usage:

```bash
clojure -A:template -Tdeps-new create :template com.acme.project/cool-lib :name myusername/mynewproject
```

Looks for `com/acme/project/cool_lib/template.edn` on the classpath (based on the `:template` alias) and,
if present, uses that template to create a project, in `mynewproject`.

Available options:
* `:template` (required) -- symbol (or string) identifying the template to use,
* `:name` (required) -- symbol (or string) identifying the project name to create,
* `:target-dir` -- string (or symbol) identifying the directory in which to create the project; defaults to the trailing portion of the qualified project name.
* `:overwrite` -- indicate whether an existing directory should be overwritten (added to), deleted, or prevent creation of the project; defaults to `nil` (prevents creation of the project); `:delete` means delete the existing directory and then create the project; any other truthy value means overlay the project on the existing directory.

The following optional keys can be provided to override defaults in the template:
* `:artifact/id` -- the `artifact-id` to use in the `pom.xml` file; defaults to the trailing portion of the qualified project name,
* `:developer` -- the capitalized version of your current username,
* `:group/id` -- the `group-id` to use in the `pom.xml` file; defaults the leading portion of the qualified project name, prefixed by `net.clojars.` if it does not already contain a `.`,
* `:main` -- the trailing portion of the qualified project name,
* `:name` -- the qualified project name (if the original `:name` was not a qualified symbol, e.g., `foo`, then this will be `foo/foo`),
* `:now/date` -- the current date, formatted as `yyyy-MM-dd`,
* `:now/year` -- the current year (four digits),
* `:raw-name` -- the original project name as supplied on the command-line (which may be unqualified),
* `:scm/domain` -- `"github.com"` unless the leading portion of the qualified project name suggests this should be hosted on `gitlab` or `bitbucket`,
* `:scm/user` -- the leading portion of the qualified project name, with known SCM hosts removed,
* `:scm/repo` -- the trailing portion of the qualified project name
* `:top` -- the leading portion of the qualified project name, with known SCM hosts removed.
* `:user` -- your current username,
* `:version` -- the version string; defaults to `"0.1.0-SNAPSHOT"`.

For all unqualified keys that have a string as a value, two additional substitutions
are available in templates:
* `:<key>/file` -- the key's value as a filename (`/`-separated, with `_`),
* `:<key>/ns` -- the key's value as a namespace (dot-separated, with `-`).
_For some keys this may make no sense, but it is intended as a convenience where you know you want a filename or namespace._

# License

Copyright © 2021 Sean Corfield

Distributed under the Eclipse Public License version 1.0.
