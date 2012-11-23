(ns palletops.ec2.meta)

(def instance-types
  {:m1.small
   {:ram (int (* 1024 1.7)) ; GiB memory
    :cpus [{:cores 1}]   ; 1 EC2 Compute Unit (1 virtual core with 1 EC2 Compute
                                        ; Unit)
    :disks [{:size 160}]                ; GB instance storage
    :32-bit true
    :64-bit true
    :io :moderate
    :ebs-optimised false}

   :m1.large
   {:ram (int (* 1024 7.5))      ; GiB memory
    :cpus [{:cores 2}]     ; 4 Compute Units (2 virtual cores with 2 EC2 Compute
                                        ; Units each)
    :disks [{:size 850}]                ; GB instance storage
    :64-bit true
    :io :high
    :ebs-optimised 500}                 ; Mbps

   :m1.xlarge
   {:ram (int (* 1024 15))       ; GiB memory
    :cpus [{:cores 4}]     ; 8 Compute Units (4 virtual cores with 2 EC2 Compute
                                        ; Units each)
    :disks [{:size 1690}]               ; GB instance storage
    :64-bit true
    :io :high
    :ebs-optimised 1000}                ; Mbps

   :m2.xlarge
   {:ram (int (* 1024 17.1))    ; GiB of memory
    :cpus [{:cores 2}]    ; 6.5 Compute Units (2 virtual cores with 3.25 Compute
                                        ; Units each)
    :disks [{:size 420}]                ; GB of instance storage
    :64-bit true
    :io :moderate
    :ebs-optimised false}

   :m2.2xlarge
   {:ram (int (* 1024 34.2))     ; GiB of memory
    :cpus [{:cores 4}]     ; 13 Compute Units (4 virtual cores with 3.25 Compute
                                        ; Units each)
    :disks [{:size 850}]                ; GB of instance storage
    :64-bit true
    :io :high
    :ebs-optimised false}

   :c1.medium
   {:ram (int (* 1024 1.7))        ; GiB of memory
    :cpus [{:cores 2}]       ; 5 Compute Units (2 virtual cores with 2.5 Compute
                                        ; Units each)
    :disks [{:size 350}]                ; GB of instance storage
    :32-bit true
    :64-bit true
    :io :moderate
    :ebs-optimised false}

   :c1.xlarge
   {:ram (int (* 1024 7))                     ; GiB of memory
    :cpus [{:cores 8}] ; 20 Compute Units (8 virtual cores with 2.5 Compute
                       ; Units each)
    :disks [{:size 1690}]               ; GB of instance storage
    :64-bit true
    :io :high
    :ebs-optimised false}

   :cc1.4xlarge
   {:ram (int (* 1024 23))                 ; Gb of memory
    :cpus [{:cores 4}{:cores 4}]     ; 33.5 Compute Units (2 x Intel Xeon X5570,
                                        ; quad-core Nehalem architecture)
    :disks [{:size 1690}]               ; GB of instance storage
    :64-bit true
    :io :very-high                      ; (10 Gigabit Ethernet)
    :ebs-optimised false}

   :cc2.8xlarge
   {:ram (int (* 1024 60.5))        ; GiB of memory
    :cpus [{:cores 8}{:cores 8}] ; 88 EC2 Compute Units (2 x Intel Xeon E5-2670,
                                        ;eight-core Sandy Bridge architecture)
    :disks [{:size 3370}]               ; GB of instance storage
    :64-bit true
    :io :very-high                      ; (10 Gigabit Ethernet)
    :ebs-optimised false}

   :cg1.4xlarge
   {:ram (int (* 1024 22))             ; GiB of memory
    :cpus [{:cores 4}{:cores 4}] ; 33.5 EC2 Compute Units (2 x Intel Xeon X5570,
                                        ; quad-core "Nehalem" architecture)
                                        ; 2 x NVIDIA Tesla "Fermi" M2050 GPUs
    :disks [{:size 1690}]               ; GB of instance storage
    :64-bit true
    :io :very-high                      ; (10 Gigabit Ethernet)
    :ebs-optimised false}})
