# Changes

* v0.4.2 -- 2021-11-13
  * Clean up `template` template: remove JAR/deploy mentions; add test that validates `template.edn`. #20.
  * Update `build-clj` to v0.5.4 and

* v0.4.1 728d512 -- 2021-11-08
  * Clarify coordinates for templates in **More General Usage** in the `README`.
  * Update `build-clj` to v0.5.3 and `tools.build` to v0.6.5.

* v0.4.0 0fb18a6 -- 2021-10-11
  * Add `:raw` mode for copying non-text assets and/or suppressing textual substitution.
  * Document alternative substitution delimiters (added in v0.3.0).
  * Update `tools.build` to v0.6.1 515b334 (for non-replacement on some non-text files).
  * Update `build-clj` to v0.5.2 (`:transitive` support).

* v0.3.3 00192bc -- 2021-10-01
  * Update `template`'s generated `README` to correct how to use the generated project.

* v0.3.2 3e68761 -- 2021-09-28
  * Add clarification on licensing (copied from `clj-new`'s `README`).
  * Update `build-clj` to v0.5.0 (which exposes the various `default-*` functions).

* v0.3.1 e4e5bc0 -- 2021-09-22
  * Update `build-clj` to v0.4.0 (which adds `install` task).

* v0.3.0 419420c -- 2021-09-19
  * Address #8 by adding generation of a `template` project.
  * Fixes a bug in `:transform` handling while copying files that were not explicitly listed.
  * Fixes a bug in the group/artifact ID used in generated `build.clj` files.

* v0.2.1 411e687 -- 2021-09-17
  * Update `build-clj` in `app` and `lib` to v0.3.1 (which uses updated `tools.build` and `deps-deploy` versions).

* v0.2.0 d3e1caa -- 2021-09-16
  * Address #12 by switching all templates to `tools.build` (from `depstar`).
  * Update `.gitignore` template files (includes change of LSP database location).

* v0.1.0 089d868 -- 2021-08-18
  * Initial tagged release.

## Work done prior to v0.1.0

* Address #10 by adding `pom` function to generate a `pom.xml` file into the specified directory.
* Address #5 by adding support for `:data-fn` and `:template-fn` (but need to document these).
* Address #1 by allowing templates to be overlaid on an existing (target) directory if `:overwrite` is any truthy value, except for `:delete` which means to delete an existing (target) directory and then create the project from scratch.
