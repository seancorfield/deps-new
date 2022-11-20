# Project Names and Variables

The project name provided to `deps-new` via the `:name` argument is used to derive
the default value of a number of variables that are then available for substituion
when a template is generated.

In general, any string of the form `{{<text>}}` where `<text>` matches one of the
built-in variables or any command-line argument, will be replaced by the value of
that variable or argument.

The value supplied via `:name` is used for the `{{raw-name}}` substitution in
templates.

If the project name is not a qualified symbol, it will be turned into one by
repeating the name as both the "namespace" and the "name" portion of the
symbol, e.g., `:name my-lib` will be treated as `my-lib/my-lib` for the
purposes of deriving the remaining variables. If the project name is a
qualified symbol, it will be used as-is.

We will use a project name of `com.acme/my.cool-lib` as the illustration for the
following derivation examples.

If `:target-dir` is not provided on the command-line, it will default to the
trailing portion of the project name, e.g., `"my.cool-lib"` (as a string).

All of the variables listed below can be overridden on the command-line by passing named
arguments that match the variable name, e.g., `:version '"1.2.3"'` to override `{{version}}`.

## Derived Variables

The following variables are derived from the (qualified) project name, again
assuming `com.acme/my.cool-lib` as the example `:name` parameter.

* `{{artifact/id}}` -- `"my.cool-lib"`,
* `{{group/id}}` -- `"com.acme"`; note: if the leading portion of the project name does not contain a `.` then a prefix of `"net.clojars."` will be added so as to help your project conform to the [Clojars Verified Group Names policy](https://github.com/clojars/clojars-web/wiki/Verified-Group-Names),
* `{{main}}` -- `"my.cool-lib"`,
* `{{name}}` -- `"com.acme/my.cool-lib"`,
* `{{raw-name}}` -- `"com.acme/my.cool-lib"`; note: this is the original value of the `:name` command-line argument,
* `{{scm/domain}}` -- `"github.com"` because `com.acme` does not indicate a known SCM domain; see **SCM Domains** below for more details,
* `{{scm/user}}` -- `"acme"`; note: this is the leading portion of the project name, with known SCM hosts removed -- see below -- and any leading `com.` or `org.` removed,
* `{{scm/repo}}` -- `"my.coo.lib"`,
* `{{top}}` -- `"com.acme"`; note: this is the leading portion of the project name, with known SCM hosts removed -- see below.

## Environmental Variables

The following variables are "derived" from the template and/or your execution environment:

* `{{description}}` -- this is usually specified as `:description` in the `template.edn` file but will default to `"FIXME: my new <template> project."` otherwise,
* `{{developer}}` -- the capitalized version of your current username,
* `{{now/date}}` -- the current date, formatted as `yyyy-MM-dd`,
* `{{now/year}}` -- the current year (four digits),
* `{{user}}` -- your current username,
* `{{version}}` -- the version string; defaults to `"0.1.0-SNAPSHOT"`.

## Additional Variables

The keys in the `template.edn` file are also available as variable substitutions where they are
not overridden via the variables above. For example, if `template.edn` contained `:developer`, it
would be ignored because there is a built-in variable `{{developer}}`. This allows template
authors to provide their own defaults for variables that are unique to their template, which can
then be overridden via command-line arguments.

Any other command-line arguments provided also become available as variables, e.g., `:foo/bar 42`
will become the variable `{{foo/bar}}` with a value of `"42"`.

Finally, for all unqualified variable names that have a string as the underlying value,
two additional variables are available in templates:

* `{{<key>/file}}` -- the key's value as a filename (`/`-separated, with `_`), e.g., `{{top/file}}` and `{{main/file}}` would be `"com/acme"` and `"my/cool_lib"` for our example, and `{{raw-name/file}}` would be `"com/acme/my/cool_lib"`,
* `{{<key>/ns}}` -- the key's value as a namespace (dot-separated, with `-`), e.g., `{{top/ns}}` and `{{main/ns}}` would still be `"com.acme"` and `"my.cool-lib"` for our example, but `{{raw-name/ns}}` would be `"com.acme.my.cool-lib"`,.

All derived variables listed above have strings as their underlying value by default but variables
provided via `template.edn` or via the command-line may have non-string values, which will not be
subject to this additional `/file` and `/ns` processing. Variables with qualified names, i.e., which
contain `/`, will also not be subject to this processing, even if they have a string value.

_For some unqualified variables this additional processing may make no sense, but it is intended as a convenience where you know you want either a filename or namespace._

## SCM Domains

`deps-new` "knows about" the [same SCM services as `tools.deps.alpha`](https://clojure.org/reference/deps_and_cli#_git), including GitHub, GitLab, and others and treats project names that contain a
reverse domain name based on those sites in a special manner.

If the project name begins with the reverse domain name of one of the supported SCM services,
that prefix is removed from the values of
`{{scm/user}}` and `{{top}}`.

In other words, a project name of `io.gitlab.myname/myproject`
will cause `"gitlab.com"` to be selected for `{{scm/domain}}` and both `{{scm/user}}` and `{{top}}`
will be `"myname"`. Similarly, for the other services listed in the [Deps and CLI Reference](https://clojure.org/reference/deps_and_cli#_git).

A project name of `com.acme/myproject` will cause `"github.com"` to be selected for
`{{scm/domain}}`, `{{scm/user}}` will be `"acme"`, and `{{top}}` will be `"com.acme"`.

In all three cases, `{{scm/repo}}` and `{{main}}` will be `"myproject"`. These variables can all
be overridden individually on the command-line.
