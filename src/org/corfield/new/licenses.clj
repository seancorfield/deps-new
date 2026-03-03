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
    - `:licenses`, specifies where to get license info (see below)
    - `:license/id`, the SPDX identifier of the license
    - `:license/name`, the name of the license,
    - `:license/url`, the URL associated with the license,
    - `:license/text`, the full text of the license.
  The returned map will always contain the above keys, though their values
  may indicate a license attribute was not found. An exception is thrown
  when a license is not found or the `:licenses` value is invalid.

  By default license info is retrieved from the SPDX jar so as to not require
  internet access. Alternatively use the `:licenses` option which may be:
    - :jar, to get license info from the SPDX jar (default behavior),
    - :cache, to get license info from SPDX API and build an incremental cache,
    - :full-cache, to get license info from a full local cache of all SPDX
                   licenses, which adds a noticeable delay upon first run.
  The last two values above use the local cache provided by the SPDX official
  library and respectively build it incrementally or download a full copy of all
  licenses upon first run. The cache resides in the user cache directory (i.e.
  `${XDG_CACHE_HOME}` or `${HOME}/.cache`). The etag of a license is checked
  after a certain interval from last run (24 hours by default), and retrieved
  if it's stale. For more details on the cache behavior and configuration see the
  [SPDX library documentation](https://github.com/spdx/Spdx-Java-Library)."
  [{:keys [licenses], opts-id :license/id
    :or {licenses :jar opts-id default-license-id} :as opts}]
  (let [string-id (if (str/blank? (str opts-id))
                    default-license-id
                    (str opts-id))
        {:keys [id name text see-also]}
        (sl/id->info string-id {:include-large-text-values? true})
        missing (fn [fieldname]
                  (format "*No %s for \"%s\" in SPDX *" fieldname id))]
    (case licenses
      :jar (when (str/blank? (System/getProperty "org.spdx.useJARLicenseInfoOnly"))
             (System/setProperty "org.spdx.useJARLicenseInfoOnly" (str true)))
      :cache nil ;; incremental cache
      :full-cache (sl/init!)
      (throw (ex-info ":licenses must be :jar, :cache, or :full-cache." opts)))
    (if id
      {:licenses     licenses
       :license/id   id
       :license/name (or name             (missing "name"))
       :license/url  (or (first see-also) (missing "URL"))
       :license/text (or text             (missing "text"))}
      (throw (ex-info (format "License with id \"%s\" not found" string-id) opts)))))
