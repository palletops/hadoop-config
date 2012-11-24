{:dev
 {:dependencies
  [[org.cloudhoist/pallet "0.8.0-SNAPSHOT" :classifier "tests"]
   [ch.qos.logback/logback-classic "1.0.0"]]
  :repositories
  {"sonatype" "https://oss.sonatype.org/content/repositories/releases/"}
  :test-selectors {:default (complement :live-test)
                   :live-test :live-test
                   :all (constantly true)}}}
