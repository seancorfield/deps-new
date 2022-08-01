(ns play.ground
  "FIXME: my new org.corfield.new/scratch project.")

(defn exec
  "Invoke me with clojure -X play.ground/exec"
  [opts]
  (println "exec with" opts))

(defn -main
  "Invoke me with clojure -M -m play.ground"
  [& args]
  (println "-main with" args))
