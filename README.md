# deps-new

The next generation of `clj-new`. Work-in-progress!

Intended to be installed as a "tool" (Clojure CLI 1.10.3.933 or later).

```bash
clojure -Ttools install com.github.seancorfield/deps-new '{:sha "..."}' :as deps-new

clojure -Tdeps-new create :template app :target-dir example
```

Currently the parameters are hardcoded and there's only that one template. Watch this space!

# License

Copyright © 2021 Sean Corfield

Distributed under the Eclipse Public License version 1.0.
