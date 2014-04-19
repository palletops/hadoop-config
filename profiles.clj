{:dev {:dependencies [[com.palletops/pallet "0.8.0-RC.9" :classifier "tests"]
                      [ch.qos.logback/logback-classic "1.0.0"]],
       :plugins [[lein-pallet-release "0.1.6"]],
       :pallet-release
       {:url "https://pbors:${GH_TOKEN}@github.com/palletops/hadoop-config.git",
        :branch "master"}}}
