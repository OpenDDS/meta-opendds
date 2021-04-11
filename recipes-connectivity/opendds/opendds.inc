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

RDEPENDS_${PN}-dev += "coreutils perl"

DDS_SRC_BRANCH ??= "master"
SRC_URI = "\
	git://github.com/objectcomputing/OpenDDS.git;protocol=https;branch=${DDS_SRC_BRANCH} \
        file://dds_custom.mwc \
"

# Versions of OCI and Doc group TAO to be used in this version of OpenDDS
# See for values of the variables 'oci_tao_version' and 'doc_tao_version' in the 'configure'
# file in the root of the OpenDDS project.
OCI_TAO_VERSION = "2.2a"
DOC_TAO3_VERSION = "6.5.12"
DOC_TAO3_VERSION = "7.0.1"
DOC_TAO_VERSION_DIR = "${@d.getVar("DOC_TAO_VERSION").replace('.','_')}"
DOC_TAO3_VERSION_DIR = "${@d.getVar("DOC_TAO3_VERSION").replace('.','_')}"

SRC_URI += "${@bb.utils.contains('PACKAGECONFIG', \
                                 'doc-group3', \
                                 'https://github.com/DOCGroup/ACE_TAO/releases/download/ACE+TAO-${DOC_TAO3_VERSION_DIR}/ACE+TAO-${DOC_TAO3_VERSION}.tar.gz;name=doc_tao3;subdir=git', \
                                 'http://download.ociweb.com/TAO-${OCI_TAO_VERSION}/ACE+TAO-${OCI_TAO_VERSION}_with_latest_patches_NO_makefiles.tar.gz;name=oci_tao;subdir=git', \
                                 d)}"



SRC_URI[opendds.md5sum] = "35a0907fd6d6b9c29a5c4718e8c35624"
SRC_URI[opendds.sha256sum] = "2f0600fb1bdeea0f8e8b6d1720f0552d20d5a3ea983c1c329095f9940dfa402c"
SRC_URI[oci_tao.md5sum] = "a21185584ca0a2785c8b3d04d5d60f3a"
SRC_URI[oci_tao.sha256sum] = "ed36f4f3b74ee1872b834b678db52de476aba738ca66f62c3d046d8ff9b3a72f"
SRC_URI[doc_tao3.md5sum] = "e79c5e5d00422a05ba116cab971f9667"
SRC_URI[doc_tao3.sha256sum] = "0cb51273368955c457febed6559c12ef70c6b7dd18a912ead3e07dbdfa520354"


S = "${WORKDIR}/git"

# Set the build directory to be the source directory
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

PACKAGECONFIG += "doc-group3"
PACKAGECONFIG[doc-group] = "--doc-group"
PACKAGECONFIG[doc-group3] = "--doc-group3"

OECONF ??= ""
OECONF_append = " \
    --prefix=${prefix} \
    --no-tests \
    --no-backup \
     --workspace=${WORKDIR}/dds_custom.mwc \
"

OECONF_append = "${PACKAGECONFIG_CONFARGS}"

OECONF_append_class-target = " \
    --host-tools=${STAGING_BINDIR_NATIVE}/DDS_HOST_ROOT \
    --target=linux-cross \
    --target-compiler=${S}/${HOST_PREFIX}g++ \
"
OECONF_append_class-native = " \
    --target=linux \
    --host-tools-only \
"
OECONF_append_class-nativesdk = " \
    --compiler=${S}/${HOST_PREFIX}g++ \
    --target=linux \
    --host-tools-only \
"

do_configure() {
    ./configure ${OECONF}
}

do_install_append_class-target() {
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
}

do_install_append_class-native() {
    rm ${D}${datadir}/dds/bin/opendds_idl
    rm ${D}${datadir}/ace/bin/ace_gperf
    rm ${D}${datadir}/ace/bin/tao_idl
}

do_install_append_class-nativesdk() {
    ln -sf ${bindir}/opendds_idl ${D}${datadir}/dds/bin/opendds_idl
    ln -sf ${bindir}/ace_gperf ${D}${datadir}/ace/bin/ace_gperf
    ln -sf ${bindir}/tao_idl ${D}${datadir}/ace/bin/tao_idl
}

FILES_${PN}-dev += "${datadir}"

BBCLASSEXTEND = "native nativesdk"
