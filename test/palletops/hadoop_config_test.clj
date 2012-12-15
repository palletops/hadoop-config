(ns palletops.hadoop-config-test
  (:use
   clojure.test
   clojure.pprint
   palletops.hadoop-config
   [pallet.compute.node-list :only [make-node]]
   [pallet.test-utils :only [test-session]]
   [palletops.ec2.meta :only [instance-types]]))

(deftest m1-small-os-size-model-test
  (let [result (first
                ((os-size-model)
                 (test-session
                  {:server
                   {:node (make-node "nn" "gg" "123.123.12.12" :ubuntu
                                     :hardware (:m1.small instance-types))}})))]
    (is (= (merge
            {:hardware (:m1.small instance-types) :roles nil}
            {:config.vm.ram 1740
             :config.vm.cores 1
             :config.vm.free-disk 150
             :config.vm.disk-size 160
             :config.os.size 300
             :config.os.cache 288
             :config.vm.application-ram 1142
             :config.vm.free-ram 1430
             :kernel.vm.overcommit_pc 25
             :kernel.vm.overcommit 1
             :kernel.vm.swapiness 0
             :kernel.fs.file-max 10240
             })
           result))
    (is (= [:default-ram :os-size-base :total-cores :total-disk :free-disk
            :os-cache :file-descriptors :file-descriptors-small :swapiness
            :swapiness-small :overcommit :overcommit-small :free-ram
            :applicaton-ram]
           (-> result meta :rules)))))

(deftest m1-small-default-node-config-test
  (let [node {:node (make-node "nn" "gg" "123.123.12.12" :ubuntu
                               :hardware (:m1.small instance-types))
              :roles #{:datanode :tasktracker}}
        result (first
                ((default-node-config {})
                 (test-session
                  {:server node
                   :service-state [node]})))]
    (is (= {
            :dfs.permissions.enabled true
            :dfs.datanode.du.reserved 45N
            :dfs.datanode.max.xcievers 4096
            :fs.trash.interval 1440
            :hadoop.rpc.socket.factory.class.ClientProtocol ""
            :hadoop.rpc.socket.factory.class.JobSubmissionProtocol ""
            :hadoop.rpc.socket.factory.class.default
            "org.apache.hadoop.net.StandardSocketFactory"
            :io.compression.codecs
            "org.apache.hadoop.io.compress.DefaultCodec,org.apache.hadoop.io.compress.GzipCodec"
            :io.file.buffer.size 65536
            :kernel.fs.file-max 10240
            :kernel.vm.overcommit 1
            :kernel.vm.overcommit_pc 25
            :kernel.vm.swapiness 0
            :mapred.compress.map.output true
            :mapred.map.tasks.speculative.execution true
            :mapred.output.compression.type "BLOCK"
            :mapred.reduce.parallel.copies 10
            :mapred.reduce.tasks 5
            :mapred.reduce.tasks.speculative.execution false
            :mapred.submit.replication 10
            :mapred.tasktracker.map.tasks.maximum 2
            :mapred.tasktracker.reduce.tasks.maximum 1
            :config.os.cache 288
            :config.os.size 300
            :config.childtask.mx 284
            :config.datanode.mx 96
            :config.task.mx 854
            :config.tasktracker.mx 192
            :config.vm.application-ram 1142
            :config.vm.cores 1
            :config.vm.disk-size 160
            :config.vm.free-disk 150
            :config.vm.free-ram 1430
            :config.vm.ram 1740
            :tasktracker.http.threads 46}
           result))
    (is (= [:mixed-datanode-tasktracker
            :mixed-datanode-tasktracker-du
            :mixed-datanode-tasktracker-small
            :total-child-process-size
            :child-process-size]
           (-> result meta :rules)))))

(deftest cc2-8xlarge-os-size-model-test
  (is (= {:roles nil
          :hardware (:cc2.8xlarge instance-types)
          :config.vm.free-ram 61588
          :config.vm.application-ram 49258
          :kernel.fs.file-max 65535
          :kernel.vm.overcommit 0
          :config.os.cache 12330
          :config.vm.ram 61952
          :config.vm.cores 16
          :config.vm.free-disk 3360
          :config.vm.disk-size 3370
          :config.os.size 300
          :kernel.vm.swapiness 0}
         (first
          ((os-size-model)
           (test-session
            {:server
             {:node (make-node "nn" "gg" "123.123.12.12" :ubuntu
                               :hardware (:cc2.8xlarge instance-types))}}))))))

(deftest cc2-8xlarge-default-node-config-test
  (let [node {:node (make-node "nn" "gg" "123.123.12.12" :ubuntu
                               :hardware (:cc2.8xlarge instance-types))
              :roles #{:datanode :tasktracker}}]
    (is (= {
            :dfs.datanode.du.reserved 1008N
            :dfs.datanode.max.xcievers 4096
            :dfs.permissions.enabled true,
            :fs.trash.interval 1440
            :hadoop.rpc.socket.factory.class.ClientProtocol ""
            :hadoop.rpc.socket.factory.class.JobSubmissionProtocol ""
            :hadoop.rpc.socket.factory.class.default
            "org.apache.hadoop.net.StandardSocketFactory"
            :io.compression.codecs
            "org.apache.hadoop.io.compress.DefaultCodec,org.apache.hadoop.io.compress.GzipCodec"
            :io.file.buffer.size 65536
            :kernel.fs.file-max 65535
            :kernel.vm.overcommit 0
            :kernel.vm.swapiness 0
            :mapred.compress.map.output true
            :mapred.map.tasks.speculative.execution true
            :mapred.output.compression.type "BLOCK"
            :mapred.reduce.parallel.copies 10
            :mapred.reduce.tasks 5
            :mapred.reduce.tasks.speculative.execution false
            :mapred.tasktracker.map.tasks.maximum 32
            :mapred.tasktracker.reduce.tasks.maximum 9
            :mapred.submit.replication 10
            :config.childtask.mx 1182
            :config.datanode.mx 384
            :config.task.mx 48490
            :config.tasktracker.mx 384
            :config.os.cache 12330
            :config.os.size 300
            :config.vm.application-ram 49258
            :config.vm.cores 16
            :config.vm.disk-size 3370
            :config.vm.free-disk 3360
            :config.vm.free-ram 61588
            :config.vm.ram 61952
            :tasktracker.http.threads 46
            }
           (first
            ((default-node-config {})
             (test-session
              {:server node
               :service-state [node]})))))))
