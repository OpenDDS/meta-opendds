# Specify SHA-1 for the release to avoid constantly checking the upstream repo.

SRCREV = "4dd8c404253b8db95bfe3e98ea38a1b14ef228a8"
DDS_SRC_BRANCH = "branch-DDS-3.24"
SRC_URI = "git://github.com/OpenDDS/OpenDDS.git;protocol=https;branch=${DDS_SRC_BRANCH};name=opendds"

require opendds.inc

DOC_TAO2_VERSION = "6.5.19"

DOC_TAO2_SHA256SUM = "e0d5faf9ec6fa457747776293eab2c71502109b6655de0e62f30dace04af809c"

DOC_TAO2_URI = "https://github.com/DOCGroup/ACE_TAO/releases/download/ACE+TAO-${@'${DOC_TAO2_VERSION}'.replace('.','_')}/ACE+TAO-src-${DOC_TAO2_VERSION}.tar.gz"

ACE_TAO_URI = "${DOC_TAO2_URI}" 
ACE_TAO_SHA256SUM = "${DOC_TAO2_SHA256SUM}" 

SRC_URI += "${ACE_TAO_URI};name=ace_tao;unpack=0;subdir=git"
SRC_URI[ace_tao.sha256sum] = "${ACE_TAO_SHA256SUM}"
