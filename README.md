# deps-new

The next generation of `clj-new`. Work-in-progress!

Intended to be installed as a "tool" (Clojure CLI 1.10.3.933 or later).

```bash
clojure -Ttools install com.github.seancorfield/deps-new '{:sha "..."}' :as deps-new

clojure -Tdeps-new create :template app :name myusername/mynewapp
```

Currently there's only that one template. Watch this space!

Available options:
* `:template` (required) -- symbol (or string) identifying template to use,
* `:name` (required) -- symbol (or string) identifying the project name to create,
* `:target-dir` -- string (or symbol) identifying the directory in which to create the project; defaults to the trailing portion of the project name.

The following optional keys can be provided to override defaults in the template:
* `:artifact/id` -- the `artifact-id` to use in the `pom.xml` file; defaults to the trailing portion of the project name,
* `:developer` -- the capitalized version of your current username,
* `:group/id` -- the `group-id` to use in the `pom.xml` file; defaults the leading portion of the project name, prefixed by `net.clojars.` if it does not contain a `.`,
* `:main` -- the trailing portion of the project name,
* `:name` -- the qualified project name,
* `:now/date` -- the current date, formatted as `yyyy-MM-dd`,
* `:now/year` -- the current year (four digits),
* `:raw-name` -- the project name as supplied on the command-line (which may be unqualified),
* `:scm/domain` -- `"github.com"` unless the leading portion of the project name indicates this should be hosted on `gitlab` or `bitbucket`,
* `:scm/user` -- the leading portion of the project name, with known SCM hosts removed,
* `:scm/repo` -- the trailing portion of the project name
* `:top` -- the leading portion of the project name, with known SCM hosts removed.
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
