# Specify SHA-1 for the release to avoid constantly checking the upstream repo.

SRCREV = "bf1b1df276f41baa2f85de103e95fe3f94b4dcc6"
DDS_SRC_BRANCH = "branch-DDS-3.20"

OCI_TAO_VERSION = "2.2a"
DOC_TAO2_VERSION = "6.5.16"
DOC_TAO3_VERSION = "7.0.6"

OCI_TAO_SHA256SUM = "bc51c94495fd9d9dd43f4d86405f67a7a8a646e752b104f50243c9cefaed2b6c"
DOC_TAO2_SHA256SUM = "c10a89cae08bf9b6d7bda166a1e35d3a737b726544c276b20b3cc7eb9c75a363"
DOC_TAO3_SHA256SUM = "590506ec126fd07b9b63dcdf86175d1b5c42c82b7f9fd180b6bf4ffcb8f95457"

require opendds.inc

python () {
    if d.getVar('ACE_TAO_OPTION') == '--doc-group':
        d.setVar('ACE_TAO_FILE', '${DOC_TAO2_FILE}')
        d.setVar('ACE_TAO_URI', '${DOC_TAO2_URI}')
        d.setVar('ACE_TAO_SHA256SUM', '${DOC_TAO2_SHA256SUM}')

    elif d.getVar('ACE_TAO_OPTION') == '--doc-group3':
        d.setVar('ACE_TAO_FILE', '${DOC_TAO3_FILE}')
        d.setVar('ACE_TAO_URI', '${DOC_TAO3_URI}')
        d.setVar('ACE_TAO_SHA256SUM', '${DOC_TAO3_SHA256SUM}')
}

do_install:append:class-native() {
    # Prepare HOST_ROOT expected by DDS for target build
    mkdir -p ${D}${bindir}/DDS_HOST_ROOT/ACE_wrappers/bin
    mkdir -p ${D}${bindir}/DDS_HOST_ROOT/bin
    ln -sr ${D}${bindir}/opendds_idl ${D}${bindir}/DDS_HOST_ROOT/bin/opendds_idl
    ln -sr ${D}${bindir}/ace_gperf ${D}${bindir}/DDS_HOST_ROOT/ACE_wrappers/bin/ace_gperf
    ln -sr ${D}${bindir}/tao_idl ${D}${bindir}/DDS_HOST_ROOT/ACE_wrappers/bin/tao_idl
}
