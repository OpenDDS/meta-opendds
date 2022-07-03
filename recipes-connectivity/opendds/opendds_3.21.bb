SUMMARY = "OpenDDS"
DESCRIPTION = "OpenDDS is an open source C++ implementation of the Object Management Group (OMG) Data Distribution Service (DDS)"
HOMEPAGE = "https://opendds.org"
BUGTRACKER = "https://github.com/objectcomputing/OpenDDS/issues"
SECTION = "networking"

LICENSE = "OpenDDS"
LIC_FILES_CHKSUM = "file://LICENSE;md5=11ee76f6fb51f69658b5bb8327c50b11"

inherit autotools

DEPENDS += "\
    cmake-native \
    opendds-native \
    perl-native \
"

RDEPENDS:${PN}-dev += "coreutils perl"

# Versions of OCI and Doc group TAO to be used in this version of OpenDDS
# See for values of the variables 'oci_tao_version' and 'doc_tao_version' in the 'configure'
# file in the root of the OpenDDS project.
DOC_TAO3_VERSION = "7.0.8"
DOC_TAO3_VERSION_DIR = "${@d.getVar("DOC_TAO3_VERSION").replace('.','_')}"

SRC_URI = "\
	https://github.com/objectcomputing/OpenDDS/releases/download/DDS-${PV}/OpenDDS-${PV}.tar.gz;name=opendds;subdir=opendds-${PV};striplevel=1 \
	https://github.com/DOCGroup/ACE_TAO/releases/download/ACE+TAO-${DOC_TAO3_VERSION_DIR}/ACE+TAO-${DOC_TAO3_VERSION}.tar.gz;name=doc_tao3;destsuffix=opendds-${PV} \
        file://dds_custom.mwc \
"

UPSTREAM_CHECK_URI = "https://github.com/objectcomputing/OpenDDS/releases/"

SRC_URI[opendds.sha256sum] = "418e88df55507164c7133cff362447ee51429686ec683bb3d763e2414622cdef"
SRC_URI[doc_tao3.md5sum] = "bbe94a64ad22263ba0f29be1cbb1479d"
SRC_URI[doc_tao3.sha256sum] = "616c3780512a6f9951e82473956106904a84df3b4d718c7c77540aaec07bb86b"

# Set the build directory to be the source directory
# kind of work around for the MPC build system?
B = "${S}"

do_unpack_extra() {
    # the configure script does not support arguments to the cross compiler
    # but the bitbake toolchain needs those
    # create shell scripts which inject the arguments into the calls
    cc_bin=${S}/${HOST_PREFIX}gcc
    echo '#!/bin/sh' > ${cc_bin}
    echo "${CC} \"\$@\"" >> ${cc_bin}
    chmod +x ${cc_bin}

    cxx_bin=${S}/${HOST_PREFIX}g++
    echo '#!/bin/sh' > ${cxx_bin}
    echo "${CXX} \"\$@\"" >> ${cxx_bin}
    chmod +x ${cxx_bin}

    ar_bin=${S}/${HOST_PREFIX}ar
    echo '#!/bin/sh' > ${ar_bin}
    echo "${AR} \"\$@\"" >> ${ar_bin}
    chmod +x ${ar_bin}
}
addtask unpack_extra after do_unpack before do_patch

PACKAGECONFIG += "${@bb.utils.filter('DISTRO_FEATURES', 'ipv6', d)}"

PACKAGECONFIG[ipv6] = "--ipv6,--no-ipv6,"
PACKAGECONFIG[ishapes] = "--qt --qt-include=${STAGING_INCDIR},,qtbase"

OECONF ??= ""
OECONF:append = " \
    --prefix=${prefix} \
    --doc-group3 \
    --no-tests \
    --no-backup \
    --no-debug \
    --ace=../ACE_wrappers \
    --std=c++11 \
    --workspace=${WORKDIR}/dds_custom.mwc \
"

OECONF:append = "${PACKAGECONFIG_CONFARGS}"

OECONF:append:class-target = " \
    --host-tools=${STAGING_BINDIR_NATIVE}/.. \
    --target=linux-cross \
    --target-compiler=${S}/${HOST_PREFIX}g++ \
"
OECONF:append:class-native = " \
    --target=linux \
    --host-tools-only \
"
OECONF:append:class-nativesdk = " \
    --compiler=${S}/${HOST_PREFIX}g++ \
    --target=linux \
    --host-tools-only \
"

do_configure() {
    ./configure ${OECONF}
}

do_install:append:class-target() {
    rm ${D}${datadir}/dds/dds/Version.h
    install -m 644 ${D}${includedir}/dds/Version.h ${D}${datadir}/dds/dds

    sed -i -e s:${D}/::g ${D}${datadir}/dds/dds-devel.sh

    # workaround: /usr/share/dds/dds/idl/IDLTemplate.txt should be placed into target sysroot
    install -d ${D}${datadir}/dds/dds/idl
    install -m 644 ${S}/dds/idl/IDLTemplate.txt ${D}${datadir}/dds/dds/idl

    for shared_lib in ${D}${libdir}/*.so.*; do
        if [ -f $shared_lib ]; then
            baselib=$(basename $shared_lib)
            shortlib=$(echo $baselib | sed 's/.so.*//')
            extn=$(echo $baselib | sed -n 's/^[^.]*\.so//p')
            extn=$(echo $extn | sed 's/[^. 0-9]*//g')
            while [ -n "$extn" ]; do
                extn=$(echo $extn | sed 's/\.[^.]*$//')
                ln -sf $baselib ${D}${libdir}/$shortlib.so$extn
            done
        fi
    done

   if [ "${@bb.utils.filter('PACKAGECONFIG', 'ishapes', d)}" ]; then
     install -d ${D}${bindir}
     install -m 755 ${B}/examples/DCPS/ishapes/ishapes ${D}${bindir}
   fi
}

do_install:append:class-native() {
    rm ${D}${datadir}/dds/bin/opendds_idl
    rm ${D}${datadir}/ace/bin/ace_gperf
    rm ${D}${datadir}/ace/bin/tao_idl
    # Prepare HOST_ROOT expected by DDS for target build
    install -d ${D}${bindir}/DDS_HOST_ROOT/ACE_wrappers/bin
    install -d ${D}${bindir}/DDS_HOST_ROOT/bin
    ln -sr ${D}${bindir}/opendds_idl ${D}${bindir}/DDS_HOST_ROOT/bin/opendds_idl
    ln -sr ${D}${bindir}/ace_gperf ${D}${bindir}/DDS_HOST_ROOT/ACE_wrappers/bin/ace_gperf
    ln -sr ${D}${bindir}/tao_idl ${D}${bindir}/DDS_HOST_ROOT/ACE_wrappers/bin/tao_idl
}


do_install:append:class-nativesdk() {
    ln -sf ${bindir}/opendds_idl ${D}${datadir}/dds/bin/opendds_idl
    ln -sf ${bindir}/ace_gperf ${D}${datadir}/ace/bin/ace_gperf
    ln -sf ${bindir}/tao_idl ${D}${datadir}/ace/bin/tao_idl
}

INSANE_SKIP:${PN} += "dev-so"

FILES_SOLIBSDEV = ""
FILES:${PN} += "${libdir}/*.so"
FILES:${PN}-dev += "${datadir}"

BBCLASSEXTEND = "native nativesdk"
