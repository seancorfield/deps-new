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

## Programmatic Transformation
