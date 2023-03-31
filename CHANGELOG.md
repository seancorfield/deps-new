# Changes

* v0.5.0.1 c4bbcfc -- 2023-03-31
  * Fix [#47](https://github.com/seancorfield/deps-new/issues/47) by adding `:only` to template project specs.

* v0.5.0 48bf01e -- 2023-01-31
  * Fix [#43](https://github.com/seancorfield/deps-new/issues/43) by updating `tools.build` to v0.9.2.
  * Address [#42](https://github.com/seancorfield/deps-new/issues/42) by removing `build-clj` wrapper (from `app` and `lib` so far) and using plain `tools.build`.
  * Fix [#39](https://github.com/seancorfield/deps-new/issues/39) by using `.tmpl` file extension for `.clj` and `.edn` resource files in templates.
  * Fix [#37](https://github.com/seancorfield/deps-new/issues/37) by using `tools.deps`'s git provider for [inferring coordinates from lib names](https://clojure.org/reference/deps_and_cli#_git).

* v0.4.13 879c4eb -- 2022-08-02
  * Merge PR [#33](https://github.com/seancorfield/deps-new/pull/33) making `deps-new` compatible with Babashka -- [@borkdude](https://github.com/borkdude).
  * Address [#32](https://github.com/seancorfield/deps-new/issues/32) by adding `:scratch` variable, defaulting to `"scratch"`.

* v0.4.12 37aa288 -- 2022-06-10
  * Bump `deps-new` in `template` to non-broken version.

* v0.4.11 aa172ea -- 2022-06-10
  * Fix git coordinates for `tools.build` #31 (thank you @dpassen).
  * Update default Clojure version to 1.11.1.
  * Update various deps.
  * Add `babashka.cli` example to README (and `ns` metadata to support that).
  * Update README to link to Deps and Cli Reference > Quoting keys and values (for Powershell users).
  * Update `build-clj` to v0.8.2 for underlying updates.
  * Update `tools.build` to v0.8.2 for various enhancements and bug fixes.

* v0.4.10 was broken, sorry!

* v0.4.9 ba30a76 -- 2022-01-04
  * Update `build-clj` to v0.6.6 for another log4j2 update.

* v0.4.8 a059d98 -- 2021-12-23
  * Update `tools.build` to v0.7.4 and `build-clj` to v0.6.5 for bug fixes and enhancements.

* v0.4.7 8fd6af0 -- 2021-12-22
  * Update `tools.build` to v0.7.3 and `build-clj` to v0.6.4 for bug fixes, enhancements, and another log4j2 update.

* v0.4.6 acc1e86 -- 2021-12-15
  * Update `build-clj` to v0.6.3 for another log4j2 update.

* v0.4.5 d2b4cda -- 2021-12-13
  * Update `tools.build` to v0.7.2 and `build-clj` to v0.6.2 (for bug fixes/enhancements in `tools.build`).

* v0.4.4 287c8c9 -- 2021-12-10
  * Update `build-clj` to v0.6.1 (for updated log4j2 dependency) and use "slim" dependency for `app` template.

* v0.4.3 3b96da4 -- 2021-11-28
  * Address #17 by adding `:only` option to copy just named files.
  * Update `build-clj` to v0.5.5 and `tools.build` to v0.6.8.

* v0.4.2 1ec7e62 -- 2021-11-13
  * Clean up `template` template: remove JAR/deploy mentions; add test that validates `template.edn`. #20.
  * Update `build-clj` to v0.5.4 and `tools.build` to v0.6.5.

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
