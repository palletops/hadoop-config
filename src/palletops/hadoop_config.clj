(ns palletops.hadoop-config
  "Hadoop configuration library."
  (:use
   [clojure.algo.monads :only [m-map]]
   [clojure.string :only [split]]
   [clojure.tools.logging :only [debugf]]
   [palletops.locos :only [defrules deep-merge apply-productions !_]]
   [pallet.crate :only [def-plan-fn nodes-with-role target target-node]]
   [pallet.debug :only [assertf]]
   [pallet.node :only [hardware]]))

;;; Some static defaults, that have no dependent configuration values,
;;; and are not dependent on install location, daemon location, etc.
(def static-defaults
  {
   :fs.trash.interval 1440        ; should trash be on or off by default?
   :io.file.buffer.size 65536     ; maybe this should be checked vs hw page size
   :dfs.permissions.enabled true
   :mapred.map.tasks.speculative.execution true
   :mapred.reduce.tasks.speculative.execution false

   :mapred.reduce.parallel.copies 10
   :mapred.reduce.tasks 5
   :mapred.submit.replication 10
   :mapred.compress.map.output true
   :mapred.output.compression.type "BLOCK"})

(def hadoop-class-details
  {:hadoop.rpc.socket.factory.class.default
   "org.apache.hadoop.net.StandardSocketFactory"
   :hadoop.rpc.socket.factory.class.ClientProtocol ""
   :hadoop.rpc.socket.factory.class.JobSubmissionProtocol ""
   :io.compression.codecs               ; LZO compression?
   (str
    "org.apache.hadoop.io.compress.DefaultCodec,"
    "org.apache.hadoop.io.compress.GzipCodec")})

;;; http://www.flumotion.net/doc/flumotion/manual/en/trunk/html/section-configuration-system.html
;;; intel paper on tuning hadoop
;;; http://allthingshadoop.com/2010/04/28/map-reduce-tips-tricks-your-first-real-cluster/
;;; http://blog.cloudera.com/blog/2009/03/configuration-parameters-what-can-you-just-ignore/

;;; Using the job streaming interface spawns extra processes so affects memory
;;; size?

;;; Kernel memory? need to estimate how much memory to leave for the kernel
;;; and file buffers, etc

;;; vm.swapiness depends on size of machine?
;;; vm.overcommit depends on size of machine?

;;; vm file descriptors limit 64K
;;; vm epoll descriptor limit 4096

;; system wide in /etc/sysctl.conf:  fs.file-max = 65535
;; process specific in /etc/security/limits.conf:  nofile 65535

;;; 1K per descriptor http://serverfault.com/questions/330795/what-are-the-ramifications-of-increasing-the-maximum-of-open-file-descriptors so that would be 64Mb for file descriptors


;;; vm read ahead buffer size 1024 to 2048


;;; dfs.namenode.handler.count default 10, up to 64 (affects namenode memory)
;;; mapred.job.tracker.handler.count default 10, up to 64 (affects jt memory)

;;; dfs.datanode.handler.count default 3, up to 8 (affects datanode memory)

;;; tasktracker.http.threads 40 to 50

;;; hdfs block size 128Mb or 256Mb

;;;  ## map/reduce

;;; mapred.tasktracker.{map/reduce}.tasks.maximum, should usually be set in the
;;; range of (cores_per_node)/2 to 2x(cores_per_ node), especially for large
;;; clusters.  -- I would think this would depend on io vs cpu requirements

;;; io.sort.factor, should be set to a sufficiently large value (for example,
;;; 100)

;;;  ## map

;;; io.sort.mb, defaults to 100 MB and can be set to a higher level, such as 200
;;; MB. (affects map task)

;;; o.sort.record. percent, which defaults to 0.05, should be adjusted according
;;; to the key-value pair size of the particular Hadoop job.

;;;  mapred.compress.map.output and mapred.output.compress, should be enabled
;;;  (especially for large clusters and large jobs). In addition, it is
;;;  recommended to evaluate LZO as the compression codec (as specified by
;;;  mapred.map.output. compression.codec and mapred.output. compression.codec).

;;;  mapred.reduce.parallel.copier, defaults to 5 and should be set to a larger
;;;  number in the range of 16 to 25 for large clusters. Higher values may
;;;  create I/O contention, so it is important to test for an optimal balance
;;;  for the given application. -- (is this related tp the datanode rpc thread
;;;  count?)

;;; rules for tuning the os




;;; # OS Size model

;;; The Operating System size model estimates kernel, and cache size, and
;;; some kernel tuning parameters.

;;; swapiness 0 prevents the OS swapping out application code in favour of disk
;;; cache.

;;; overcommit 0 prevents memory overcommitment.

;;; file-max is the maximum number of file descriptors.

;;; TODO: investigate if the distribution in use should be considered in the
;;; estimation.
(defrules os-size-rules
  ;; generic rules

  ;; note that variants of a rule should have matching logic vars in the pattern
  ;; to ensure that they are all applied at once.
  ^{:name :default-ram}
  [{:hardware {:ram ?r} :pallet.vm.ram !_}
   {:pallet.vm.ram ?r}]

  ^{:name :os-size-base}                ; estimate of os size
  [{:hardware {:ram ?r} :pallet.os.size !_}
   {:pallet.os.size 300}]

  ^{:name :total-cores}
  [{:hardware {:cpus ?c} :pallet.vm.cores !_}
   {:pallet.vm.cores (reduce + (map :cores ?c))}]

  ^{:name :total-disk}
  [{:hardware {:disks ?d} :pallet.vm.disk-size !_}
   {:pallet.vm.disk-size (reduce + (map :size '?d))}]

  ;; the free disk estimate needs to improve
  ;; OS, log files, metrics, etc all take disk space
  ^{:name :free-disk}
  [{:pallet.vm.disk-size ?d :pallet.vm.free-disk !_}
   {:pallet.vm.free-disk (- ?d 10) }]   ; Gb

  ^{:name :os-cache}                    ; estimate of a reasonable disk cache
  [{:pallet.vm.ram ?m :pallet.os.size ?o :pallet.os.cache !_}
   {:pallet.os.cache (int (* (- ?m ?o) 0.2))}]

  ^{:name :file-descriptors}            ; file descriptors to configure
  [{:pallet.vm.ram ?r :kernel.fs.file-max !_}
   {:kernel.fs.file-max 65535}]

  ^{:name :file-descriptors-small}
  [{:pallet.vm.ram ?r :kernel.fs.file-max !_}
   {:kernel.fs.file-max 10240}
   (< ?r 2048)]

  ^{:name :swapiness}
  [{:pallet.vm.ram ?r :kernel.vm.swapiness !_}
   {:kernel.vm.swapiness 0
    :kernel.vm.overcommit 0}]

  ^{:name :swapiness-small}
  [{:pallet.vm.ram ?r :kernel.vm.swapiness !_}
   {:kernel.vm.swapiness 0
    :kernel.vm.overcommit 0}
   (< ?r 2048)]

  ^{:name :overcommit}
  [{:pallet.vm.ram ?r :kernel.vm.overcommit !_}
   {:kernel.vm.swapiness 0}]

  ^{:name :overcommit-small}
  [{:pallet.vm.ram ?r :kernel.vm.overcommit !_}
   {:kernel.vm.overcommit 1             ; maybe only matters when using hadoop
                                        ; streaming
    :kernel.vm.overcommit_pc 25}
   (< ?r 2048)]

  ^{:name :free-ram}                    ; ram - os ram - file descriptor space
  [{:pallet.vm.free-ram !_
    :pallet.vm.ram ?r
    :pallet.os.size ?o
    :kernel.fs.file-max ?f}
   {:pallet.vm.free-ram
    (int (- ?r ?o (/ ?f 1024.0)))}]

  ^{:name :applicaton-ram}              ; free-ram - disk cache
  [{:pallet.vm.free-ram ?f :pallet.vm.application-ram !_
    :pallet.os.cache ?c}
   {:pallet.vm.application-ram
    (int (- ?f ?c))}])



(def-plan-fn os-size-model
  "Returns an estimate for the OS size on the current node. The rules can be
   passed using the :rules keyword.  The :pallet.vm.free-ram returned is an
   estimate of the amount of ram consumed by the operating system.
   The :pallet.vm.application-ram is the free ram, reduce by an estimate of
   a reasonable disk cache size.

   We use a 1K per file descriptor estimator for file descriptor size.

   Estimates can be overridden by passing a map to :overrides."
  [& {:keys [rules overrides] :or {rules os-size-rules}}]
  [target target
   node target-node
   m (m-result (merge {:roles (:roles target)
                       :hardware (hardware node)}
                      overrides))
   model (m-result (apply-productions m rules))]
  (assertf (seq model) "Failed to find an os size model for %s" m)
  (m-result model))

;;; # Node Configuration Sizing

;;; Returns a map of configuration parameters for hadoop, based on hardware
;;; sizes, and operating system size.

;;; The model is based on estimating hadoop daemon sizes, using the number of
;;; cpu's to estimate the number of map and reduce tasks, and then sharing out
;;; the available memory to these tasks.

;;; Namenode and jobtracker threads are estimated based on cluster size.
(defrules node-config-sizing-rules

  ^{:name :dedicated-namenode}
  [{:roles #{:namenode :datanode}
    :pallet.vm.application-ram ?f
    :cluster {:datanodes ?d}}
   {:pallet.namenode.mx ?f
    :dfs.namenode.handler.count (max 10 (int (* 20 (Math/log (double ?d)))))}]

  ^{:name :dedicated-jobtracker}
  [{:roles #{:jobtracker}
    :pallet.vm.application-ram ?f
    :cluster {:tasknodes ?t}}
   {:pallet.jobtracker.mx ?f
    :mapred.job.tracker.handler.count
    (max 10 (int (* 20 (Math/log (double ?t)))))}]

  ^{:name :mixed-namenode-jobtracker}
  [{:roles #{:namenode :jobtracker :datanode}
    :pallet.vm.application-ram ?f
    :cluster {:tasknodes ?t}}
   {:pallet.namenode.mx (* ?f 0.25)
    :pallet.jobtracker.mx (* ?r 0.75)}]

  ^{:name :mixed-namenode-jobtracker-small}
  [{:roles #{:namenode :jobtracker :datanode}
    :pallet.vm.application-ram ?f}
   {:pallet.namenode.mx 384
    :pallet.jobtracker.mx 384}
   (< ?f 2048)]

  ^{:name :mixed-datanode-tasktracker}
  [{:roles #{:datanode :tasktracker}
    :pallet.vm.application-ram ?f
    :pallet.vm.cores ?c
    :pallet.datanode.mx !_
    :pallet.tasktracker.mx !_}
   {:pallet.datanode.mx 384
    :pallet.tasktracker.mx 384
    :mapred.tasktracker.map.tasks.maximum (int (* 2 ?c))
    :mapred.tasktracker.reduce.tasks.maximum (max 1 (int (* 0.6 ?c)))
    :tasktracker.http.threads 46
    :dfs.datanode.max.xcievers 4096}]

  ^{:name :mixed-datanode-tasktracker-du}
  [{:roles #{:datanode :tasktracker}
    :pallet.vm.free-disk ?d
    :dfs.datanode.du.reserved !_}
   {:dfs.datanode.du.reserved (bigint (* 0.3 ?d))}] ; a guess (bytes/volume)

  ^{:name :mixed-datanode-tasktracker-small}
  [{:roles #{:datanode :tasktracker}
    :pallet.vm.application-ram ?f
    :pallet.vm.cores ?c
    :pallet.datanode.mx !_
    :pallet.tasktracker.mx !_}
   {:pallet.datanode.mx 96
    :pallet.tasktracker.mx 192}
   (< ?f 2048)]

  ^{:name :total-child-process-size}
  [{:pallet.vm.application-ram ?f
    :pallet.datanode.mx ?d
    :pallet.tasktracker.mx ?t
    :pallet.task.mx !_}
   {:pallet.task.mx (- ?f ?d ?t)}]

  ^{:name :child-process-size}
  [{:pallet.childtask.mx !_
    :pallet.task.mx ?t
    :mapred.tasktracker.map.tasks.maximum ?m
    :mapred.tasktracker.reduce.tasks.maximum ?r}
   {:pallet.childtask.mx (int (/ ?t (+ ?m ?r)))}]


  ;; replication
  ^{:name :replication}
  [{:cluster {:datanodes ?d}
    :dfs.replication !_}
   {:dfs.replication 3}]

  ^{:name :replication-single}
  [{:cluster {:datanodes ?d}
    :dfs.replication !_}
   {:dfs.replication 1}
   (< ?d 4)]

  ^{:name :replication-two}
  [{:cluster {:datanodes ?d}
    :dfs.replication !_}
   {:dfs.replication 2}
   (< ?d 10) (> ?d 3)]

  ;; TODO
  ;; dfs.block.size
  )

(def-plan-fn node-count-for-role
  [role]
  [nodes (nodes-with-role role)]
  (m-result [role (count nodes)]))

(def-plan-fn cluster-role-counts
  []
  [count-vecs (m-map node-count-for-role
                     [:jobtracker :namenode :tasktracker :datanode])]
  (m-result (into {} count-vecs)))

(def-plan-fn node-config
  "Plan function to return a configuration map for a node. The rules can
   be passed with the :rules keyword."
  [os-size & {:keys [rules] :or {rules node-config-sizing-rules}}]
  [target target
   node target-node
   cluster (cluster-role-counts)
   m (m-result (deep-merge
                {:roles (:roles target)
                 :hardware (hardware node)
                 :cluster cluster}
                os-size))
   config (m-result (apply-productions m rules))]
  (assertf (seq config) "Failed to find a node config for %s" m)
  (m-result (debugf "node-config %s" config))
  (m-result config))

(defn- dotted-keys->nested-maps
  "Takes a map with key names containing dots, and turns them into nested maps."
  [m re]
  (reduce
   (fn [m [k v]]
     (if (re-matches re (name k))
       (-> m
           (dissoc k)
           (assoc-in (->> (split (name k) #"\.") (map keyword)) v))
       m))
   m m))

(defn nested-maps->dotted-keys
  ([m prefix]
     (reduce
      (fn [result [key value]]
        (if (map? value)
          (merge result
                 (nested-maps->dotted-keys value (str prefix (name key) ".")))
          (assoc result (keyword (str prefix (name key))) value)))
      {}
      m))
  ([m]
     (nested-maps->dotted-keys m "")))

(def-plan-fn default-node-config
  "An all in one configuration function. You can pass a map of property values
  as `config`.

  Use of this function is optional - you can call the constituent functions
  if you need more flexibility."
  [config & {:keys [os-rules node-config-rules]
             :or {os-rules os-size-rules
                  node-config-rules node-config-sizing-rules}}]
  [os-size (os-size-model :rules os-rules :overrides config)
   node-config (node-config os-size :rules node-config-rules)]
  (m-result (->
             (deep-merge
              static-defaults hadoop-class-details os-size node-config)
             (dotted-keys->nested-maps #"pallet\..*")
             (dissoc :hardware :roles :cluster)
             (with-meta (meta node-config)))))
