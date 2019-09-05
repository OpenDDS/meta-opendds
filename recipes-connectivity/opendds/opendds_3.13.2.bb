# Specify SHA-1 for the release to avoid constantly checking the upstream repo.

SRCREV = "3274828ccb99aad571fab1f9c117128715debba7"
DDS_SRC_BRANCH = "branch-DDS-3.13"

require opendds.inc

do_install_append_class-native() {
    rm ${D}${datadir}/dds/dds/Version.h
    cp ${D}${includedir}/dds/Version.h ${D}${datadir}/dds/dds

    rm ${D}${datadir}/ace/lib/libTAO_IDL_[FB]E.so
}

do_install_append_class-nativesdk() {
    rm ${D}${datadir}/dds/dds/Version.h
    cp ${D}${includedir}/dds/Version.h ${D}${datadir}/dds/dds

    rm ${D}${datadir}/ace/lib/libTAO_IDL_[FB]E.so
}
