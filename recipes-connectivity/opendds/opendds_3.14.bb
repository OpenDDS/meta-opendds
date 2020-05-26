# Specify SHA-1 for the release to avoid constantly checking the upstream repo.

SRCREV = "16b426a3ccdba5360b06d9bff625c56dc04e511f"
DDS_SRC_BRANCH = "branch-DDS-3.14"

require opendds.inc

do_install_append_class-native() {
    rm ${D}${datadir}/dds/dds/Version.h
    cp ${D}${includedir}/dds/Version.h ${D}${datadir}/dds/dds

    rm ${D}${datadir}/ace/lib/libTAO_IDL_[FB]E.so

    # Prepare HOST_ROOT expected by DDS for target build
    mkdir -p ${D}${bindir}/DDS_HOST_ROOT/ACE_wrappers/bin
    mkdir -p ${D}${bindir}/DDS_HOST_ROOT/bin
    ln -sr ${D}${bindir}/opendds_idl ${D}${bindir}/DDS_HOST_ROOT/bin/opendds_idl
    ln -sr ${D}${bindir}/ace_gperf ${D}${bindir}/DDS_HOST_ROOT/ACE_wrappers/bin/ace_gperf
    ln -sr ${D}${bindir}/tao_idl ${D}${bindir}/DDS_HOST_ROOT/ACE_wrappers/bin/tao_idl
}

do_install_append_class-nativesdk() {
    rm ${D}${datadir}/dds/dds/Version.h
    cp ${D}${includedir}/dds/Version.h ${D}${datadir}/dds/dds

    rm ${D}${datadir}/ace/lib/libTAO_IDL_[FB]E.so
}
