# Specify SHA-1 for the release to avoid constantly checking the upstream repo.

SRCREV = "8f28fcea3cb786e01ac0f51f8a8e12854d3e9a5c"
DDS_SRC_BRANCH = "branch-DDS-3.22"
SRC_URI = "git://github.com/objectcomputing/OpenDDS.git;protocol=https;branch=${DDS_SRC_BRANCH};name=opendds"

require opendds.inc

OCI_TAO_VERSION = "2.2a"
DOC_TAO2_VERSION = "6.5.18"
DOC_TAO3_VERSION = "7.0.8"

OCI_TAO_SHA256SUM = "373ea7b78d521b9b8250e201fc6364ca9d5a8ef398b7b09872571858d526a5aa"
DOC_TAO2_SHA256SUM = "c10a89cae08bf9b6d7bda166a1e35d3a737b726544c276b20b3cc7eb9c75a363"
DOC_TAO3_SHA256SUM = "590506ec126fd07b9b63dcdf86175d1b5c42c82b7f9fd180b6bf4ffcb8f95457"

OCI_TAO_URI = "http://download.objectcomputing.com/TAO-${OCI_TAO_VERSION}/ACE+TAO-${OCI_TAO_VERSION}_with_latest_patches_NO_makefiles.tar.gz"
DOC_TAO2_URI = "https://github.com/DOCGroup/ACE_TAO/releases/download/ACE+TAO-${@'${DOC_TAO2_VERSION}'.replace('.','_')}/ACE+TAO-src-${DOC_TAO2_VERSION}.tar.gz"
DOC_TAO3_URI = "https://github.com/DOCGroup/ACE_TAO/releases/download/ACE+TAO-${@'${DOC_TAO3_VERSION}'.replace('.','_')}/ACE+TAO-src-${DOC_TAO3_VERSION}.tar.gz"

ACE_TAO_OPTION ??= ""
ACE_TAO_URI ??= "${OCI_TAO_URI}" 
ACE_TAO_SHA256SUM ??= "${OCI_TAO_SHA256SUM}" 

python () {
    if d.getVar('ACE_TAO_OPTION') == '--doc-group':
        d.setVar('ACE_TAO_URI', '${DOC_TAO2_URI}')
        d.setVar('ACE_TAO_SHA256SUM', '${DOC_TAO2_SHA256SUM}')
    elif d.getVar('ACE_TAO_OPTION') == '--doc-group3':
        d.setVar('ACE_TAO_URI', '${DOC_TAO3_URI}')
        d.setVar('ACE_TAO_SHA256SUM', '${DOC_TAO3_SHA256SUM}')
}

SRC_URI += "${ACE_TAO_URI};name=ace_tao;unpack=0;subdir=git"
SRC_URI[ace_tao.sha256sum] = "${ACE_TAO_SHA256SUM}"

do_install:append:class-native() {
    # Prepare HOST_ROOT expected by DDS for target build
    mkdir -p ${D}${bindir}/DDS_HOST_ROOT/ACE_wrappers/bin
    mkdir -p ${D}${bindir}/DDS_HOST_ROOT/bin
    ln -sr ${D}${bindir}/opendds_idl ${D}${bindir}/DDS_HOST_ROOT/bin/opendds_idl
    ln -sr ${D}${bindir}/ace_gperf ${D}${bindir}/DDS_HOST_ROOT/ACE_wrappers/bin/ace_gperf
    ln -sr ${D}${bindir}/tao_idl ${D}${bindir}/DDS_HOST_ROOT/ACE_wrappers/bin/tao_idl
}
