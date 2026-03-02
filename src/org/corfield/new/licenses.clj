;; copyright (c) 2026 Francois Rey, all rights reserved

(ns org.corfield.new.licenses
  "Light wrapper around the [clj-spdx](https://github.com/pmonks/clj-spdx)
  library to access the license repository managed by [SPDX](https://spdx.dev/)."
  (:require [clojure.string :as str]
            [spdx.licenses :as sl]))


(def default-license-id "EPL-1.0")
(defn id->license
  "Retrieve from [SPDX](https://spdx.dev/) library the full info and text of
  a license identified from option `:license/id`, or `nil` if not found.
  If `:license/id` is not provided it defaults to `\"EPL-1.0\"`, a license
  traditionally used in clojure projects.
  Other popular licenses are `\"MIT\"`, `\"Apache-2.0\"`, `\"EPL-2.0\"`, or any
  identifier found in the [SPDX license list](https://spdx.org/licenses/).
  Returns a map with the following entries:
    - `:license/id`, the SPDX identifier of the license,
    - `:license/name`, the name of the license,
    - `:license/url`, the URL associated with the license,
    - `:license/text`, the full text of the license.

  The SPDX official library provides a local cache that should be built
  incrementally in the user cache directory (i.e. `${XDG_CACHE_HOME}` or
  `${HOME}/.cache`). By default the etag of a license is checked after a
  certain interval, and retrieved if it's stale. See the official
  [SPDX library documentation](https://github.com/spdx/Spdx-Java-Library) about
  the cache behavior and configuration.
  However the incremental behavior of the cache needs to be verified as there
  has been reports of performance issues due to the unexpected download of the
  full set of licenses from SPDX, which appears to take a lot more than just
  downloading the 22Mb it represents.
  Until this is validated the licenses info included in the SPDX JAR is used by
  default. Even though it may be out of date with SPDX listing, this should be
  fit for purpose because licenses don't change very often.
  To change that default behavior use the `:licenses` option which may be:
    - :jar, to get license info from the SPDX jar (default behavior),
    - :cache, to get license info from SPDX API and build an incremental cache,
    - :full-cache, to get license info from a full local cache of all SPDX
                   licenses, which adds a noticeable delay upon first run. "
  [{:keys [licenses], opts-id :license/id
    :or {licenses :jar opts-id default-license-id} :as opts}]
  (let [string-id (if (str/blank? (str opts-id))
                    default-license-id
                    (str opts-id))
        {:keys [id name text see-also]}
        (sl/id->info string-id {:include-large-text-values? true})
        missing (fn [fieldname]
                  (format "* (No %s for %s in SPDX) *" fieldname id))]
    (case licenses
      :jar (when (str/blank?
                  (System/getProperty "org.spdx.useJARLicenseInfoOnly"))
             (System/setProperty "org.spdx.useJARLicenseInfoOnly" (str true)))
      :full-cache (sl/init!)
      :cache nil ;; incremental cache
      (throw (ex-info ":licenses must be :jar, :cache, or :full-cache." opts)))
    (when id
      {:license/id   id
       :license/name (or name             (missing "name"))
       :license/url  (or (first see-also) (missing "URL"))
       :license/text (or text             (missing "text"))})))
