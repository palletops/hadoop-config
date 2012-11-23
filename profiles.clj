{:dev
 {:dependencies
  [[org.cloudhoist/pallet-vmfest "0.2.1-SNAPSHOT"]
   [org.cloudhoist/pallet "0.8.0-SNAPSHOT" :classifier "tests"]
   [org.cloudhoist/pallet-lein "0.5.2"]
   [com.palletops/hadoop-book-example "0.1.0-SNAPSHOT"]
   [ch.qos.logback/logback-classic "1.0.0"]]
  :repositories
  {"sonatype" "https://oss.sonatype.org/content/repositories/releases/"}
  :test-selectors {:default (complement :live-test)
                   :live-test :live-test
                   :all (constantly true)}}
 :jclouds
 {:dependencies
  [[org.cloudhoist/pallet-jclouds "1.5.0-SNAPSHOT"]
   [org.jclouds.provider/aws-ec2 "1.5.3"]
   [org.jclouds.provider/aws-s3 "1.5.3"]
   [org.jclouds.driver/jclouds-slf4j "1.5.3"
    ;; the declared version is old and can overrule the resolved version
    :exclusions [org.slf4j/slf4j-api]]
   [org.jclouds.driver/jclouds-sshj "1.5.3"]]}}
