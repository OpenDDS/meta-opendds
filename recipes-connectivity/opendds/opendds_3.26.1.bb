# Specify SHA-1 for the release to avoid constantly checking the upstream repo.

SRCREV = "3d7f89e8243fbf382ce6374f900489e76894d4f9"
DDS_SRC_BRANCH = "branch-DDS-3.26"
SRC_URI = "\
    git://github.com/OpenDDS/OpenDDS.git;protocol=https;branch=${DDS_SRC_BRANCH};name=opendds \
    ${@bb.utils.contains('PACKAGECONFIG', 'ishapes', 'file://0001-adding-the-ishapes-demo.patch', '', d)} \
    file://0002-fixing-DoAll-testing-DoAll.patch \
"

require opendds.inc

DOC_TAO2_VERSION = "6.5.20"
DOC_TAO3_VERSION = "7.1.1"

DOC_TAO2_SHA256SUM = "7546d11785dc7e03b578c82f39cc9ddb8b574685453e8479ac8765827e9936ad"
DOC_TAO3_SHA256SUM = "69ec3a8afffc16694a65550776b6fa8fc60846c5e3530bb7d74191bf4a43c711"

DOC_TAO2_URI = "https://github.com/DOCGroup/ACE_TAO/releases/download/ACE+TAO-${@'${DOC_TAO2_VERSION}'.replace('.','_')}/ACE+TAO-src-${DOC_TAO2_VERSION}.tar.gz"
DOC_TAO3_URI = "https://github.com/DOCGroup/ACE_TAO/releases/download/ACE+TAO-${@'${DOC_TAO3_VERSION}'.replace('.','_')}/ACE+TAO-src-${DOC_TAO3_VERSION}.tar.gz"

ACE_TAO_OPTION ??= ""
ACE_TAO_URI ??= "${DOC_TAO2_URI}"
ACE_TAO_SHA256SUM ??= "${DOC_TAO2_SHA256SUM}"

python () {
    if d.getVar('ACE_TAO_OPTION') == '--doc-group3':
        d.setVar('ACE_TAO_URI', '${DOC_TAO3_URI}')
        d.setVar('ACE_TAO_SHA256SUM', '${DOC_TAO3_SHA256SUM}')
}

SRC_URI += "${ACE_TAO_URI};name=ace_tao;unpack=0;subdir=git"
SRC_URI[ace_tao.sha256sum] = "${ACE_TAO_SHA256SUM}"
