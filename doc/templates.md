# Writing Templates

A minimal template is a directory that contains:
* `template.edn` -- describing the template; can be `{}`,
* `root` -- a folder containing all of the files and folders that make up the template.

By default, `deps-new` simply copies the contents of the `root` folder to the `:target-dir`
and substitutes any `{{opt}}` strings for the corresponding `:opt` value (computed from
the project name or supplied on the command-line).
