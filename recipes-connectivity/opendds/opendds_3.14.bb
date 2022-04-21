# Specify SHA-1 for the release to avoid constantly checking the upstream repo.

SRCREV = "16b426a3ccdba5360b06d9bff625c56dc04e511f"
DDS_SRC_BRANCH = "branch-DDS-3.14"

OCI_TAO_VERSION = "2.2a"
DOC_TAO2_VERSION = "6.5.8"

OCI_TAO_SHA256SUM = "bc51c94495fd9d9dd43f4d86405f67a7a8a646e752b104f50243c9cefaed2b6c"
DOC_TAO2_SHA256SUM = "4d1f0b12553f777804b4b79935db515e7212f24aa07b7616aa914a22f8579019"

require opendds.inc

python () {
    if d.getVar('ACE_TAO_OPTION') == '--doc-group':
        d.setVar('ACE_TAO_FILE', '${DOC_TAO2_FILE}')
        d.setVar('ACE_TAO_URI', '${DOC_TAO2_URI}')
        d.setVar('ACE_TAO_SHA256SUM', '${DOC_TAO2_SHA256SUM}')
}

do_install:append:class-native() {
    # Prepare HOST_ROOT expected by DDS for target build
    mkdir -p ${D}${bindir}/DDS_HOST_ROOT/ACE_wrappers/bin
    mkdir -p ${D}${bindir}/DDS_HOST_ROOT/bin
    ln -sr ${D}${bindir}/opendds_idl ${D}${bindir}/DDS_HOST_ROOT/bin/opendds_idl
    ln -sr ${D}${bindir}/ace_gperf ${D}${bindir}/DDS_HOST_ROOT/ACE_wrappers/bin/ace_gperf
    ln -sr ${D}${bindir}/tao_idl ${D}${bindir}/DDS_HOST_ROOT/ACE_wrappers/bin/tao_idl
}
