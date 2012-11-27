(ns palletops.hadoop-config-test
  (:use
   clojure.test
   clojure.pprint
   palletops.hadoop-config
   [pallet.compute.node-list :only [make-node]]
   [pallet.test-utils :only [test-session]]
   [palletops.ec2.meta :only [instance-types]]))

(deftest m1-small-os-size-model-test
  (is (= (merge
          {:hardware (:m1.small instance-types) :roles nil}
          {:pallet.vm.ram 1740
           :pallet.vm.cores 1
           :pallet.vm.free-disk 150
           :pallet.vm.disk-size 160
           :pallet.os.size 300
           :pallet.os.cache 288
           :pallet.vm.application-ram 1142
           :pallet.vm.free-ram 1430
           :kernel.vm.overcommit_pc 25
           :kernel.vm.overcommit 1
           :kernel.vm.swapiness 0
           :kernel.fs.file-max 10240
           })
         (first
          ((os-size-model)
           (test-session
            {:server
             {:node (make-node "nn" "gg" "123.123.12.12" :ubuntu
                               :hardware (:m1.small instance-types))}}))))))

(deftest m1-small-default-node-config-test
  (let [node {:node (make-node "nn" "gg" "123.123.12.12" :ubuntu
                               :hardware (:m1.small instance-types))
              :roles #{:datanode :tasktracker}}]
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
            :pallet.childtask.mx 284
            :pallet.datanode.mx 96
            :pallet.os.cache 288
            :pallet.os.size 300
            :pallet.task.mx 854
            :pallet.tasktracker.mx 192
            :pallet.vm.application-ram 1142
            :pallet.vm.cores 1
            :pallet.vm.disk-size 160
            :pallet.vm.free-disk 150
            :pallet.vm.free-ram 1430
            :pallet.vm.ram 1740
            :tasktracker.http.threads 46
            }
           (first
            ((default-node-config {})
             (test-session
              {:server node
               :service-state [node]})))))))

(deftest cc2-8xlarge-os-size-model-test
  (is (= {:roles nil
          :hardware (:cc2.8xlarge instance-types)
          :pallet.vm.free-ram 61588
          :pallet.vm.application-ram 49258
          :kernel.fs.file-max 65535
          :kernel.vm.overcommit 0
          :pallet.os.cache 12330
          :pallet.vm.ram 61952
          :pallet.vm.cores 16
          :pallet.vm.free-disk 3360
          :pallet.vm.disk-size 3370
          :pallet.os.size 300
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
            :pallet.childtask.mx 1182
            :pallet.datanode.mx 384
            :pallet.os.cache 12330
            :pallet.os.size 300
            :pallet.task.mx 48490
            :pallet.tasktracker.mx 384
            :pallet.vm.application-ram 49258
            :pallet.vm.cores 16
            :pallet.vm.disk-size 3370
            :pallet.vm.free-disk 3360
            :pallet.vm.free-ram 61588
            :pallet.vm.ram 61952
            :tasktracker.http.threads 46
            }
           (first
            ((default-node-config {})
             (test-session
              {:server node
               :service-state [node]})))))))
