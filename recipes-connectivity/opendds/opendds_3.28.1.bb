DOC_TAO2_VERSION = "6.5.20"
DOC_TAO3_VERSION = "7.1.3"

DOC_TAO2_SHA256SUM = "7546d11785dc7e03b578c82f39cc9ddb8b574685453e8479ac8765827e9936ad"
DOC_TAO3_SHA256SUM = "7d394cfcc71d0e90824fd1399f93640b4c9423016b88974ad3e4694c5301d96a"

DOC_TAO2_URI = "https://github.com/DOCGroup/ACE_TAO/releases/download/ACE+TAO-${@'${DOC_TAO2_VERSION}'.replace('.','_')}/ACE+TAO-src-${DOC_TAO2_VERSION}.tar.gz"
DOC_TAO3_URI = "https://github.com/DOCGroup/ACE_TAO/releases/download/ACE+TAO-${@'${DOC_TAO3_VERSION}'.replace('.','_')}/ACE+TAO-src-${DOC_TAO3_VERSION}.tar.gz"

DDS_SRC_BRANCH = "branch-DDS-3.28"
SRC_URI = "\
    git://github.com/OpenDDS/OpenDDS.git;protocol=https;branch=${DDS_SRC_BRANCH};name=opendds \
    ${@bb.utils.contains('PACKAGECONFIG', 'doc-group3', '${DOC_TAO3_URI};name=ace_tao;unpack=0;subdir=git', '${DOC_TAO2_URI};name=ace_tao;unpack=0;subdir=git', d)} \
    ${@bb.utils.contains('PACKAGECONFIG', 'ishapes', 'file://0001-adding-the-ishapes-demo.patch', '', d)} \
    file://0010-WIP-test-filter-tests-that-should-not-run-on-the-tar.patch \
"

require opendds.inc

SRCREV = "802922c55923db381e0a55f6e435186b60946964"
SRC_URI[ace_tao.sha256sum] = "${@bb.utils.contains('PACKAGECONFIG', 'doc-group3', '${DOC_TAO3_SHA256SUM}', '${DOC_TAO2_SHA256SUM}', d)}"
