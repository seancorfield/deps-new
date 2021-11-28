# Writing Templates

A minimal template is a directory that contains:
* `template.edn` -- describing the template; can be `{}`,
* `root` -- a folder containing all of the files and folders that make up the template.

By default, `deps-new` simply copies the contents of the `root` folder to the `:target-dir`
and substitutes any `{{opt}}` strings for the corresponding `:opt` value (computed from
the project name or supplied on the command-line).

Templates can contain a `:description` key, providing a string that will typically
appear in the generated README and/or `pom.xml` file (e.g., in the built-in `app`, `lib`,
`pom`, and `scratch` templates).

The name of the "root" folder can be overridden via a `:root` key, if you want to use
something other than `"root"` for that.

Templates can contain any additional keys you want, and those all become default values
for substitutions that can be overridden via the command-line. Keys that match those
derived from the project name or the environment should not be used -- the derived
variables will override them.

For any unqualified key computed from the project name or supplied on the command-line
that has a string as its value, an `{{opt/ns}}` version is also available that should
be suitable for use as a namespace in the generated code, and an `{{opt/file}}` version
that should be suitable for use as a filename or directory path.

## Renaming Folders

All of the files inside the "root" folder are copied to matching files in the
"target" folder. That includes file paths so `<root>/doc/intro.md` will be copied to
`<target>/doc/intro.md`. If you want some files copied to different locations you
can provide multiple folders in the template and specify how those folders should
be mapped in the `template.edn` file, under a `:transform` key:

```clojure
;; template.edn
{:transform [["resources" "resources/{{top/file}}"]]}
```

This says that the contents of the template's `resources` folder should be copied
to a subfolder within the `<target>/resources` folder. For our example `com.acme/cool-lib`
project, that would be `<target>/resources/com/acme` (since `{{top}}` would `"com.acme"`
so there will be `{{top/ns}}` and `{{top/file}}` substitutions as well).

## Renaming Files

If you wish to rename specific files as part of that copying, you can provide a hash
map as the third element of the transformation tuple, specifying how to map those
files:

```clojure
;; template.edn
{:transform
 [["src" "src/{{top/file}}"
   {"main.clj" "{{main/file}}.clj"}]
  ["test" "test/{{top/file}}"
   {"main_test.clj" "{{main/file}}_test.clj"}]]}
```

In this example, `src/main.clj` will be copied to `<target>/src/com/acme/cool_lib.clj`
and `test/main_test.clj` will be copied to `<target>/test/com/acme/cool_lib_test.clj`.
Any files in `src` (or `test`) that are not specifically listed in the hash map will
be copied as-is.

## Copying Files (Only)

As seen above, by default the entire folder is copied, with specified files renamed.
Sometimes it is convenient to copy only specified files from a folder and ignore the
other files in that folder. You can specify `:only` as the last element of the
transformation tuple:

```clojure
;; template.edn
{:transform
 [["src" "src/{{top/file}}"
   {"main.clj" "{{main/file}}.clj"}]
  ["test" "test/{{top/file}}"
   {"main_test.clj" "{{main/file}}_test.clj"}
   :only]]}
```

In this example, `src/main.clj` will be copied to `<target>/src/com/acme/cool_lib.clj`
and `test/main_test.clj` will be copied to `<target>/test/com/acme/cool_lib_test.clj`.
Any files in `src` that are not specifically listed in the hash map will
be copied as-is. Because of the `:only` option, no other files in `test` will be
copied -- just the specified ones (`main_test.clj` in this case).

## Alternative Delimiters

As indicated above, patterns like `{{opt}}` are replaced by the value of the `:opt` option
when files are copied. If you are working with template files (e.g., for Selmer), those
will contain `{{..}}` patterns that you will not want substituted if they accidentally
match your options.

For such cases, you can specify alternative delimiters as a pair (vector) of two strings
representing the open and close sections for substitution, followng the (optional) hash map of
file renamings:

```clojure
;; template.edn
{:transform
 [["src" "src/{{top/file}}"
   {"main.clj" "{{main/file}}.clj"}]
  ["test" "test/{{top/file}}"
   {"main_test.clj" "{{main/file}}_test.clj"}
   ["<<" ">>"]]]}
```

In this example, while files in `src` will use `{{opt}}` as the substitution pattern,
files in `test` will use `<<opt>>` as the substitution pattern. This is how `deps-new`
itself handles generation of the `template` project, since some files that are copied
are templates themselves that will later have substitutions applied to them.

## Suppressing Substitution & Binary Files

By default, `deps-new` passes a `:replace` option to the `copy-dir` of `tools.build`
in order to perform the substitutions of `{{opt}}`. That function skips some common
image types (`jpg`, `jpeg`, `png`, `gif`, and `bmp` as of `tools.build` v0.6.1)
but treats all other
as text and attempts to perform textual replacements -- so some binary files will not
be copied correctly. In addition, you may want to copy some files as if they were
templates and not have substitution performed.

You can suppress substitution for a specified directory of files in a template,
such as `"templates"`, by adding `:raw` as the last element of the transformation tuple
for that directory:

```clojure
;; template.edn
{:transform [["resources" "resources/{{top/file}}"]
             ["templates" "resources/{{top/file}}/templates" :raw]]}
```

In this example, files in `resources` will be treated as text (except for the common
image files noted above) and substitution
will be performed on them but files in `templates` will be copied to the specified
target as raw files -- with no substitutions (and therefore safely treated as binary files,
if appropriate).

> Note: you can specify both `:raw` and `:only` as the last elements of the transformation tuple, if needed, and they can be in either order, but they must be after the delimiter string pair if that is also specified.

## Programmatic Transformation

Sometimes you might need more than just a declarative approach and simple string
substitution. For those situations, you can declare transformation functions in
`template.edn` that will be invoked on the data and/or the EDN of the template
itself.

The keys are `:data-fn` and `:template-fn` and the values should be fully-qualified
symbols that will resolve to functions that can be invoked as follows:

* `:data-fn` -- a function that is invoked with a hash map containing all the substitution data, both derived and from the command-line, and can return additional key/value pairs that should be added to it,
* `:template-fn` -- a function that is invoked with the EDN (hash map) as the first argument and the substitution data (augmented by the result of `:data-fn` if present), and which should return the updated template EDN.

The data function could, for example, dynamically generate the content of one or more files as
new keys in the substitution map, and the template function could augment the EDN with additional
folders to copy and transform. This would allow for a template to accept command-line arguments
that controlled the inclusion of optional features in the generated project.
