# Available Options

The following options can be provided to `deps-new`:

* `:template` (required) -- symbol (or string) identifying the template to use,
* `:name` (required) -- symbol (or string) identifying the project name to create,
* `:target-dir` -- string (or symbol) identifying the directory in which to create the project; defaults to the trailing portion of the qualified project name.
* `:overwrite` -- indicate whether an existing directory should be overwritten (added to), deleted, or prevent creation of the project; defaults to `nil` (prevents creation of the project); `:delete` means delete the existing directory and then create the project; any other truthy value means overlay the project on the existing directory.

The following optional keys can be provided to override defaults in the template:
* `:artifact/id` -- the `artifact-id` to use in the `pom.xml` file; defaults to the trailing portion of the qualified project name,
* `:description` -- a string used in the generated README and `pom.xml` files to describe the project; the default value is typically provided by `template.edn`,
* `:developer` -- the capitalized version of your current username,
* `:group/id` -- the `group-id` to use in the `pom.xml` file; defaults the leading portion of the qualified project name, prefixed by `net.clojars.` if it does not already contain a `.`,
* `:main` -- the trailing portion of the qualified project name,
* `:name` -- the qualified project name (if the original `:name` was not a qualified symbol, e.g., `foo`, then this will be `foo/foo`),
* `:now/date` -- the current date, formatted as `yyyy-MM-dd`,
* `:now/year` -- the current year (four digits),
* `:raw-name` -- the original project name as supplied on the command-line (which may be unqualified),
* `:scm/domain` -- `"github.com"` unless the leading portion of the qualified project name suggests this should be hosted on `gitlab` or another SCM provider,
* `:scm/user` -- the leading portion of the qualified project name, with known SCM hosts removed and with leading `com.` or `org.` removed,
* `:scm/repo` -- the trailing portion of the qualified project name,
* `:top` -- the leading portion of the qualified project name, with known SCM hosts removed.
* `:user` -- your current username,
* `:version` -- the version string; defaults to `"0.1.0-SNAPSHOT"`.
