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
    qtbase-native \
    qtbase \
    xerces-c \
"

RDEPENDS:${PN}-dev += "coreutils perl"

# Versions of OCI and Doc group TAO to be used in this version of OpenDDS
# See for values of the variables 'oci_tao_version' and 'doc_tao_version' in the 'configure'
# file in the root of the OpenDDS project.
DOC_TAO3_VERSION = "7.0.5"
DOC_TAO3_VERSION_DIR = "${@d.getVar("DOC_TAO3_VERSION").replace('.','_')}"

SRC_URI = "\
	https://github.com/objectcomputing/OpenDDS/releases/download/DDS-${PV}/OpenDDS-${PV}.tar.gz;name=opendds;subdir=opendds-3.19;striplevel=1 \
	https://github.com/DOCGroup/ACE_TAO/releases/download/ACE+TAO-${DOC_TAO3_VERSION_DIR}/ACE+TAO-${DOC_TAO3_VERSION}.tar.gz;name=doc_tao3;destsuffix=opendds-3.19 \
        file://dds_custom.mwc \
        file://0001-Disable-the-boost_or_cxx11-std11-is-set-at-configure.patch \
        file://0002-Disable-the-search-for-moc.patch \
"

UPSTREAM_CHECK_URI = "https://github.com/objectcomputing/OpenDDS/releases"

SRC_URI[opendds.sha256sum] = "2236d86b6629601d92b3c597fe9f2b63a4a4ac779866e11576c50e5aa95d6cb8"
SRC_URI[doc_tao3.md5sum] = "47362d44afa3d69a85d8c5f1c36ac113"
SRC_URI[doc_tao3.sha256sum] = "da10403fcd6b7a39d90a8bb2d4d5669ae71f297fe1617e6e221fa109fdff74d6"

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

DEPENDS:append_class-target = " xerces-c"
DEPENDS:append_class-dev = " libxerces-c-dev"
DEPENDS:append_class-native = " xerces-c-native"

OECONF ??= ""
OECONF:append = " \
    --prefix=${prefix} \
    --xerces3=${STAGING_DIR_TARGET}/usr \
    --doc-group3 \
    --no-tests \
    --no-backup \
    --no-debug \
    --ace=../ACE_wrappers \
    --std=c++11 \
    --workspace=${WORKDIR}/dds_custom.mwc \
"

# ${STAGING_EXECPREFIXDIR}

OECONF:append = "${PACKAGECONFIG_CONFARGS}"

OECONF:append:class-target = " \
    --qt=${RECIPE_SYSROOT}/usr \ 
    --host-tools=${STAGING_BINDIR_NATIVE}/DDS_HOST_ROOT \
    --target=linux-cross \
    --target-compiler=${S}/${HOST_PREFIX}g++ \
"
OECONF:append:class-native = " \
    --qt=${RECIPE_SYSROOT_NATIVE}/usr \ 
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

do_configure:append:class-target() {
    sed -i 's:${STAGING_BINDIR}/uic:${STAGING_BINDIR_NATIVE}/uic:g' ${S}/examples/DCPS/ishapes/GNUmakefile.ishapes
    sed -i 's:${STAGING_BINDIR}/moc:${STAGING_BINDIR_NATIVE}/moc:g' ${S}/examples/DCPS/ishapes/GNUmakefile.ishapes
    sed -i 's:${STAGING_BINDIR}/rcc:${STAGING_BINDIR_NATIVE}/rcc:g' ${S}/examples/DCPS/ishapes/GNUmakefile.ishapes
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

    # install the ishapes demo
    install -m 777 ${S}/examples/DCPS/ishapes/ishapes ${D}${bindir}
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

FILES:${PN}-dev += "${datadir}"

BBCLASSEXTEND = "native nativesdk"

INSANE_SKIP_${PN} += "dev-so"

FILES_SOLIBSDEV = ""
FILES_${PN} += "${libdir}/*.so"
