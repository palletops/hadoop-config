(defproject com.palletops/hadoop-config "0.1.1-SNAPSHOT"
  :description "Hadoop configuration library"
  :url "https://github.com/palletops/hadoop-config"
  :license {:name "All rights reserved"}
  :dependencies [[palletops/locos "0.1.0"]
                 [org.cloudhoist/pallet "0.8.0-alpha.7"]
                 [org.clojure/clojure "1.4.0"]]
  :repositories
  {"sonatype" {:url "https://oss.sonatype.org/content/repositories/releases/"}}
  :test-selectors {:default (complement :live-test)
                   :live-test :live-test
                   :all (constantly true)})
