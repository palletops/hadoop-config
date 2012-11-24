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
              :roles #{:data-node :task-tracker}}]
    (is (= {:hardware (:m1.small instance-types)
            :roles #{:data-node :task-tracker}
            :cluster {:job-tracker 0 :name-node 0 :task-tracker 1 :data-node 1}
            :kernel.vm.overcommit_pc 25
            :pallet.vm.free-ram 1430
            :pallet.vm.application-ram 1142
            :pallet.datanode.mx 96
            :kernel.fs.file-max 10240
            :kernel.vm.overcommit 1
            :pallet.task.mx 854
            :pallet.os.cache 288
            :mapred.tasktracker.reduce.tasks.maximum 1
            :pallet.tasktracker.mx 192
            :pallet.vm.ram 1740
            :pallet.vm.cores 1
            :pallet.childtask.mx 284
            :mapred.tasktracker.map.tasks.maximum 2
            :pallet.os.size 300
            :kernel.vm.swapiness 0}
           (first
            ((default-node-config)
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
              :roles #{:data-node :task-tracker}}]
    (is (= {:hardware (:cc2.8xlarge instance-types)
            :roles #{:data-node :task-tracker}
            :cluster {:job-tracker 0 :name-node 0 :task-tracker 1 :data-node 1}
            :pallet.vm.free-ram 61588
            :pallet.vm.application-ram 49258
            :pallet.datanode.mx 384
            :kernel.fs.file-max 65535
            :kernel.vm.overcommit 0
            :pallet.task.mx 48490
            :pallet.os.cache 12330
            :mapred.tasktracker.reduce.tasks.maximum 9
            :pallet.tasktracker.mx 384
            :pallet.vm.ram 61952
            :pallet.vm.cores 16
            :pallet.childtask.mx 1182
            :mapred.tasktracker.map.tasks.maximum 32
            :pallet.os.size 300
            :kernel.vm.swapiness 0}
           (first
            ((default-node-config)
             (test-session
              {:server node
               :service-state [node]})))))))
