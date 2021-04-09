# Specify SHA-1 for the release to avoid constantly checking the upstream repo.

SRCREV = "8a819227a9df24dddae52f7229c358b69ed8ef8a"
DDS_SRC_BRANCH = "branch-DDS-${PV}"

require opendds.inc

do_install_append_class-native() {
    # Prepare HOST_ROOT expected by DDS for target build
    install -d ${D}${bindir}/DDS_HOST_ROOT/ACE_wrappers/bin
    install -d ${D}${bindir}/DDS_HOST_ROOT/bin
    ln -sr ${D}${bindir}/opendds_idl ${D}${bindir}/DDS_HOST_ROOT/bin/opendds_idl
    ln -sr ${D}${bindir}/ace_gperf ${D}${bindir}/DDS_HOST_ROOT/ACE_wrappers/bin/ace_gperf
    ln -sr ${D}${bindir}/tao_idl ${D}${bindir}/DDS_HOST_ROOT/ACE_wrappers/bin/tao_idl
}
