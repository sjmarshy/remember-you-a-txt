(defproject remember-you-a-txt "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [
                 [org.clojure/clojure "1.8.0"]
                 [me.raynes/fs "1.4.6"]
                 [clj-time "0.14.0"]]
  :main ^:skip-aot remember-you-a-txt.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
