# deps-new

Create new projects for use with the Clojure CLI and `deps.edn`.

Discuss on [![Slack](https://img.shields.io/badge/slack-deps--new-yellow.svg?logo=slack)](https://clojurians.slack.com/app_redirect?channel=deps-new).

## Installation

Intended to be installed as a [tool](https://clojure.org/reference/clojure_cli#tools) (Clojure CLI 1.11.1.1149 or later).

```bash
clojure -Ttools install-latest :lib io.github.seancorfield/deps-new :as new
```

> Note: if you get an error about `No known ancestor relationship between git versions` then you probably installed [`clj-new`](https://github.com/seancorfield/clj-new) `:as new` previously, so you will need to remove that first: `clojure -Ttools remove :tool new`. `clj-new` should be installed `:as clj-new` if you want to use both tools.

_`deps-new` only supports `:local/root` and `git`-based coordinates, not Maven/Clojars coordinates. If you want an alternative that supports distributing your templates using Maven/Clojars look at [clj-new](https://github.com/seancorfield/clj-new) but bear in mind I am no longer actively maintaining that._

> Note: if you see instructions to use a template that look like `clojure -A:new some-template :name whatever` or `clojure -Tnew some-template :name whatever`, where `some-template` is not one of the built-in templates (`app`, `lib`, `pom`, `scratch`, `template`), those probably refer to [clj-new](https://github.com/seancorfield/clj-new) rather than `deps-new`. Any templates other than the built-in ones are created and maintained by the community in repositories elsewhere, so please open issues or ask questions of the appropriate maintainer (not me).

The documentation here is structured as follows:

* [Motivation](#motivation) -- why does `deps-new` exist?
* Create an [Application](#create-an-application) -- how to create a basic application project, that can build an uberjar
* Create a [Library](#create-a-library) -- how to create a basic library project, that can build a JAR and deploy to Clojars
* Create a [Template](#create-a-template) -- how to create your own `deps-new` template project
* Additional projects/files that `deps-new` can create ([scratch](#create-a-minimal-scratch-project), [`pom.xml`](#create-a-fully-fleshed-pomxml))
* [More General Usage](#more-general-usage) -- this includes links to additional documentation:
  * [Project Names and Variables](doc/names-variables.md) to see how the project name (`:name`) is used to derive the default values of all the built-in substitution variables
  * [All the Options](doc/options.md) for the full list of command-line options available when invoking `deps-new`
  * [Writing Templates](doc/templates.md) for documentation on how to write your own templates

Followed by sections listing some community `deps-new` templates, integration with Emacs, some notes about the generated `LICENSE` file, and finally how to use
`deps-new` with the Babashka CLI library.

## Motivation

`clj-new` inherently carries along all of the baggage of `lein new` and `boot new`, including a modified chunk of Leiningen itself, as well as depending on Pomegranate for loading dependencies (so as to be compatible with Leiningen and Boot), and Stencil for the variable substitution in templates. The recently-released `tools.build` library, from the core Clojure team, provides all of the functionality needed to create new projects from templates, so `deps-new` aims to provide a wrapper around `tools.build`, some standard templates "out of the box", and machinery to allow you to easily write your own templates, mostly with no code needed at all.

The `app` and `lib` templates in `deps-new` are currently almost identical to those in `clj-new`, in terms of what
they provide in generated projects, although they need no code: `deps-new` templates are primarily declarative,
using a `template.edn` file to describe how parts of the template are copied into the target project folder.

You can get help on the available functions like this:

```bash
clojure -A:deps -Tnew help/doc
```

## Create an Application

```bash
clojure -Tnew app :name myusername/mynewapp
```

Creates a directory `mynewapp` containing a new application project, with `myusername` as the
"top" namespace and `mynewapp` as the main project namespace:

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

In this new project, you can run `clojure -A:deps -T:build help/doc` to see what tasks are available
in `build.clj`. You can run the following in the freshly-generated project:

* `clojure -T:build test` -- run the tests (they'll fail until you fix them!)
* `clojure -T:build ci` -- run the tests and build the (AOT-compiled) uberjar

Or, if you used the `:build :bb` option, you can run:

* `bb tasks` -- list the available tasks
* `bb test` -- run the tests (they'll fail until you fix them!)
* `bb ci` -- run the tests and build the (AOT-compiled) uberjar

Consult the generated `README.md` file for additional details on how to run the
source code, run the tests, and build and run the uberjar.

See also [**Test Runners**](#test-runners) and
[**`build.clj` or `bb.edn`?**](#buildclj-or-bbedn) for more options.

## Create a Library

```bash
clojure -Tnew lib :name myusername/mycoollib
```

Creates a directory `mycoollib` containing a new library project, with `myusername` as the "top" namespace
and `mycoollib` as the main project namespace under that.

If you want to generate the project into a different directory than the project name, use
the `:target-dir` option to specify a path to the directory that should be created:

```bash
clojure -Tnew lib :name myusername/mycoollib :target-dir projects/newlib
```

Creates a directory `projects/newlib` containing a new library project, with `myusername` as
the "top" namespace and `mycoollib` as the main project namespace under that.

In this new project, you can run `clojure -A:deps -T:build help/doc` to see what tasks are available
in `build.clj`. You can run the following in the freshly-generated project:

* `clojure -T:build test` -- run the tests (they'll fail until you fix them!)
* `clojure -T:build ci` -- run the tests and build the library jar

Or, if you used the `:build :bb` option, you can run:

* `bb tasks` -- list the available tasks
* `bb test` -- run the tests (they'll fail until you fix them!)
* `bb ci` -- run the tests and build the library jar

Consult the generated `README.md` file for additional details on how to run
functions from the source code, run the tests, build the jar, install it
locally or deploy it to Clojars.

See also [**Test Runners**](#test-runners) and
[**`build.clj` or `bb.edn`?**](#buildclj-or-bbedn) for more options.

## Create a Template

```bash
clojure -Tnew template :name myusername/mytemplate
```

Creates a directory `mytemplate` containing a new template project, with `myusername` as the "top" namespace
and `mytemplate` as the main project namespace under that. The generated template
project will work as a template that produces a library project, but you can
change it to produce whatever you want.

If you want to generate the project into a different directory than the project name, use
the `:target-dir` option to specify a path to the directory that should be created:

```bash
clojure -Tnew template :name myusername/mytemplate :target-dir projects/newtemplate
```

Creates a directory `projects/newtemplate` containing a new library project, with `myusername` as
the "top" namespace and `mytemplate` as the main project namespace under that.

In this new project, you can run `clojure -A:deps -T:build help/doc` to see what tasks are available
in `build.clj`. You can run the following in the freshly-generated project:

* `clojure -T:build test` -- run the tests (they'll fail until you fix them!)

Consult the generated `README.md` file for additional details on how to work
with the newly-generated template project.

See also [**Test Runners**](#test-runners) for more options.

> Note: `:build :bb` is not yet supported for template projects.

## Create a Minimal "scratch" Project

If you just want a very minimal `deps.edn` project to experiment with:

```bash
clojure -Tnew scratch :name play
```

Creates a directory `play` containing an empty `deps.edn` file and `src/scratch.clj`
with a simple `exec` function (you can invoke via `clojure -X scratch/exec`) and a
simple `-main` function (you can invoke via `clojure -M -m scratch`). This is intended
to be a minimal "playground" to get started with `deps.edn` and the CLI.

If you want the `scratch.clj` file to have a different name, you can override the
default with `:scratch`:

```bash
clojure -Tnew scratch :name play :scratch ground
```

The created file will be `src/ground.clj` in the `play` folder. `:scratch` can be
a path:

```bash
clojure -Tnew scratch :name play :scratch play/ground
```

The created file will be `src/play/ground.clj` in the `play` folder.

## Create a Fully-Fleshed `pom.xml`

```bash
clojure -Tnew pom :name com.acme/cool-lib :target-dir .
```

Creates a `pom.xml` file in the current directory (_overwriting any existing file!_)
that has all the fields needed to publish a project to Clojars and have cljdoc.org
generate the documentation, e.g.,

```xml
  <groupId>com.acme</groupId>
  <artifactId>cool-lib</artifactId>
  <version>0.1.0-SNAPSHOT</version>
  <name>com.acme/cool-lib</name>
  <description>FIXME: my new org.corfield.new/pom project.</description>
  <url>https://github.com/com.acme/cool-lib</url>
  <licenses>
    <license>
      <name>Eclipse Public License 2.0</name>
      <url>https://www.eclipse.org/legal/epl-2.0</url>
    </license>
  </licenses>
...
  <scm>
    <url>https://github.com/acme/cool-lib</url>
    <connection>scm:git:https://github.com/acme/cool-lib.git</connection>
    <developerConnection>scm:git:ssh:git@github.com:acme/cool-lib.git</developerConnection>
    <tag>v0.1.0-SNAPSHOT</tag>
  </scm>
```

You should run `clojure -X:deps mvn-pom` to synchronize the `<dependencies>` from your `deps.edn` file.

## Test Runners

By default, generated projects use `clojure.test` for unit tests, and the
[Cognitect Labs Test Runner](https://github.com/cognitect-labs/test-runner)
for running tests.

As of v0.9.0, you can use the `:test-runner` command-line option to choose
[LazyTest](https://github.com/NoahTheDuke/lazytest) instead, by specifying
`:test-runner :lazytest`:

```bash
clojure -Tnew app :name myusername/mynewapp :test-runner :lazytest
```

## `build.clj` or `bb.edn`?

By default, generated projects use `build.clj` for all build-related tasks,
including running tests.

As of v0.10.0, you can use the `:build` command-line option to choose
[Babashka](https://github.com/babashka/babashka)
(`bb` tasks) instead, by specifying `:build :bb`:

```bash
clojure -Tnew app :name myusername/mynewapp :build :bb
```

This will generate a `bb.edn` file as well as a (minimal) `build.clj` file,
so you can run `bb tasks` to list available tasks, and `bb test` to run the
tests. This also offers `bb test:bb` to run the tests using Babashka itself,
to check compatibility with Babashka. Other tasks include `test:all`, `ci`,
and `ci:deploy` (for `lib` projects, which also have a local `install` task).

## More General Usage

Currently those are the only five built-in templates (`app`, `lib`, `pom`, `scratch`, and `template`).

More general usage:

```bash
clojure -A:somealias -Tnew create :template com.acme.project/cool-lib :name myusername/mynewproject
```

Looks for `com/acme/project/cool_lib/template.edn` on the classpath (based on the `:somealias` alias) and,
if present, uses that template to create a project, in `mynewproject`. Instead of `-A:somealias`, you
could use `-Sdeps` to specify the dependencies needed to make the template available:

```bash
clojure -Sdeps '{:deps {io.github.acme/templates COORDINATES}}' -Tnew create :template com.acme.project/cool-lib :name myusername/mynewproject
```

The `COORDINATES` could be something like `{:local/root "/path/to/cool-lib"}`
for a template that exists on the local filesystem, or it could be based on `:git/url`/`:git/sha` etc
for a template that exists in a `git` repository.

As of v0.7.0, if you are using Clojure 1.12 -- either as the default `:deps` in
your `deps.edn` file or via an alias, such as `-A:1.12` -- you can use a
shorter syntax for the template dependency:

```bash
clojure -A:1.12 -Tnew create :template io.github.acme/templates%com.acme.project/cool-lib :name myusername/mynewproject
```

`deps-new` will infer a `git` dependency, as `https://github.com/acme/templates`,
figure out the latest version on the default branch, check that out, and add it
to the classpath, and then proceed to use `com.acme.project/cool-lib` as above.
If you want to use a specific tag, you can use `#` to append that to the
template specification, e.g., `io.github.acme/templates%com.acme.project/cool-lib#v1.2.3`.
If the repo is structured such that the Clojure root is not the root of the
repo itself, i.e., you would normally use `:deps/root` in the coordinates, you
can specify that with an extra `%` in the template specification, after the
repo and before the actual template name, e.g., `io.github.acme/templates%lib%com.acme.project/cool-lib#v1.2.3`.
This would be equivalent to `{:deps {io.github.acme/templates {:git/tag "v1.2.3" :deps/root "lib"}}}`
(which would not be legal without `:git/sha` as well for `-Sdeps` but
`deps-new` will resolve the tag to a SHA for you).

If your template name matches the `git` "lib" name, you can omit the template
from the specification, e.g., `io.github.acme/cool-lib` would be treated as
both the implied `git` repo and also the template name, as if you had specified:
`io.github.acme/cool-lib%io.github.acme/cool-lib`.

The examples above using `-A:1.12` assume an alias like this in your `deps.edn` file:

```clojure
  :1.12 {:override-deps {org.clojure/clojure {:mvn/version "1.12.4"}}}
```

> Note: if you are on Windows, read [**Quoting keys and values**](https://clojure.org/reference/deps_and_cli#quoting) in the official **Deps and CLI Reference** documentation to understand how the above command needs to look on Powershell. Or take a look at the [Babashka CLI](#babashka-cli) library support.

> Note: because `deps-new` is based on `tools.build` and uses its file copying functions, the template must ultimately live on the filesystem, so `:local/root` and `git`-based coordinates are supported, **but Maven/Clojars coordinates are not**.

As of v0.6.0, `:src-dirs` can be used to specify a list of
directories to search for templates, in addition to the classpath.
Those directories are searched in order, and take priority over the
classpath. This allows you to have templates in a directory structure
that is outside the classpath, and also makes it easier to use
`deps-new` as a library.

See [**Project Names and Variables**](doc/names-variables.md) to see how the project name (`:name`)
is used to derive the default values of all the built-in substitution variables.
See [**All the Options**](doc/options.md) for the full list of command-line options available
when invoking `deps-new`. See [**Writing Templates**](doc/templates.md) for documentation on
how to write your own templates.

Practical.li also has an excellent
[guide to writing `deps-new` templates](https://practical.li/blog/create-deps-new-templates-for-clojure-cli-projects/).

### Templates

The following templates are available externally. If you have written a template and would like to add it to the list, please make a PR.

- [anvil-template](https://github.com/codesmith-gmbh/anvil-template) - A template for projects using anvil and blocks
- [clerk-utils](https://github.com/mentat-collective/clerk-utils/tree/main/resources/clerk_utils/custom) - Create a fully-configured Clerk notebook project
- [Clojure Live Reload HTML Server Template](https://github.com/AdamFrey/clojure-html-server-live-reload-template)
- [deps-new-lib-adoc-template](https://github.com/KingMob/deps-new-lib-adoc-template) - The default `lib` template, but using AsciiDoc
- [practical.li templates](https://github.com/practicalli/project-templates) - Well-documented, well-maintained, fully-fleshed out templates for creating applications and services (with more to come)
- [re-marfer](https://github.com/kees-/re-marfer) - A minimal re-frame SPA template
- [Hoplon project template](https://github.com/hoplon/project-template) - A template for Hoplon projects
- [clj-polyglot-app](https://github.com/behrica/clj-polyglot-app) - A template to create a polyglot app in Clojure using `devcontainer`.
- [helix-scratch](https://github.com/rafaeldelboni/helix-scratch) - A minimal helix scratch SPA Template.

## Emacs Integration

An [emacs package](https://github.com/jpe90/emacs-clj-deps-new) is available which provides a Magit-style interface to `clj-new` and `deps-new`. It includes some community templates and welcomes for recommendations for more.

## License of the generated project

All projects are generated by default with the Eclipse Public License 2.0.
The full license text goes into a `LICENSE` file and the license name and URL are also
mentioned in the generated `README.md` files.

**You are not required to use EPLv2.0 for your project!**
If you want to use another license you can pass it as a command line option, e.g.

```bash
clojure -Tnew app :name myusername/mynewapp :license/id MIT
```

The identifier to use are defined in the [SPDX license list](https://spdx.org/licenses/).
The OSI also displays the SPDX id in their [listing of OSI-approved licenses](https://opensource.org/licenses).

**You are not required to open source your generated project!** Just because the projects
are generated with an open source `LICENSE` file and have a **License** section in their
`README.md` files does not mean you need to keep that license in place, if you do not
want your project to be open source.

**Template authors can specify a default license** in their `template.edn` files using
the `:license/id` key. This allows the generated projects to automatically use
the specified license unless overridden by the user.

**Choosing a license**: the use of the Eclipse Public License (v1.0) is a tradition that
started with Leiningen's `lein new` and carried over into `boot new` and now `clj-new`.
The idea is that it's better to ensure any open source projects created have a valid
license of some sort, as a starting point, and historically most Clojure projects use
the EPLv1.0 because Clojure itself and the Contrib libraries have all used this license
for a long time. I have tended to use the [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)
in most of my open source projects, prior to working with Clojure, but see
[Prefer the MIT License](https://juxt.pro/blog/prefer-mit) for an alternative viewpoint from the
folks who wrote XTDB.

> Note: EPL-1.0 is not compatible with a lot of other open source licenses, but EPL-2.0 allows for more compatibility, and for relicensing under certain conditions. This project is EPL-2.0 but allows for relicensing under Apache-2.0.

> Note: if you incorporate any source code from other people's open source projects, be
aware of the legal implications and that you must respect whatever license _they_ have
used for that code (which _may_ require you to release your enhancements under the same
license and will, most likely, require you to include their copyright notices, etc).
_Do not copy other people's code without attribution!_

**IMPORTANT NOTES AND DISCLAIMER**
- The generation of license text is a convenience automation. In no event shall the authors or
  contributors be liable for damages arising from use of the software and generated licenses.
- The user is strongly recommended to review the generated output and amend accordingly.
  In particular some license text from SPDX may contain placeholders for various information
  such as year and copyright owner.
- For performance reasons the list of licenses is currently a local static resource.
  While it contains most licenses likely to be used, it may not reflect all licenses present
  in the [SPDX license list](https://spdx.org/licenses/). See the docstring of
  `org.corfield.new.licenses/id->license` for more details on that story.

## Babashka CLI

The [babashka CLI](https://github.com/babashka/cli) library allows you to call
an `-X` (exec) function in a more Unixy way, without writing EDN on the command
line. If you are dealing with quoting issues in your shell, this could be a
viable alternative:

```clojure
:new {:deps {org.babashka/cli {:mvn/version "0.8.67"}
             io.github.seancorfield/deps-new {:git/tag "v0.11.1"
                                              :git/sha "dd459f0"}}
      :ns-default org.corfield.new
      :exec-args {} ;; insert default arguments here
      :main-opts ["-m" "babashka.cli.exec"]}
```

This allows you to call `deps-new` on the command line as:

``` bash
$ clj -M:new app --name foo/bar --overwrite delete
```

# License

Copyright © 2021-2025 Sean Corfield

Distributed under the [Eclipse Public License 2.0](https://www.eclipse.org/legal/epl-2.0)
