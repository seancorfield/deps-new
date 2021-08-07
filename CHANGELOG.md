# Changes

We haven't reached an alpha yet!

* Address #5 by adding support for `:data-fn` and `:template-fn` (but need to document these).
* Address #1 by allowing templates to be overlaid on an existing (target) directory if `:overwrite` is any truthy value, except for `:delete` which means to delete an existing (target) directory and then create the project from scratch.
