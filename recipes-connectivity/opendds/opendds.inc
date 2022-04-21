SUMMARY = "OpenDDS is an open source C++ implementation of the Object Management Group (OMG) Data Distribution Service (DDS)"
HOMEPAGE = "https://opendds.org"

LICENSE = "OpenDDS"
LIC_FILES_CHKSUM = "file://LICENSE;md5=11ee76f6fb51f69658b5bb8327c50b11"

inherit autotools

DEPENDS += " \
    perl-native \
    cmake-native \
    opendds-native \
"

RDEPENDS:${PN}-dev += " coreutils perl"

DDS_SRC_BRANCH ??= "master"
SRC_URI = "git://github.com/objectcomputing/OpenDDS.git;protocol=https;branch=${DDS_SRC_BRANCH}"

## pre-fetch ACE/TAO to download, uses the ACE version number
OCI_TAO_FILE = "ACE+TAO-${OCI_TAO_VERSION}_with_latest_patches_NO_makefiles.tar.gz"
DOC_TAO2_FILE = "ACE+TAO-src-${DOC_TAO2_VERSION}.tar.gz"
DOC_TAO3_FILE = "ACE+TAO-src-${DOC_TAO3_VERSION}.tar.gz"

OCI_TAO_URI = "http://download.objectcomputing.com/TAO-${OCI_TAO_VERSION}/${OCI_TAO_FILE}"
DOC_TAO2_URI = "https://github.com/DOCGroup/ACE_TAO/releases/download/ACE+TAO-${@'${DOC_TAO2_VERSION}'.replace('.','_')}/${DOC_TAO2_FILE}"
DOC_TAO3_URI = "https://github.com/DOCGroup/ACE_TAO/releases/download/ACE+TAO-${@'${DOC_TAO3_VERSION}'.replace('.','_')}/${DOC_TAO3_FILE}"

ACE_TAO_FILE ??= "${OCI_TAO_FILE}"
ACE_TAO_URI ??= "${OCI_TAO_URI}"
ACE_TAO_SHA256SUM ??= "${OCI_TAO_SHA256SUM}"
ACE_TAO_OPTION ??= ""

SRC_URI += "${ACE_TAO_URI};name=ace_tao;unpack=0"
SRC_URI[ace_tao.sha256sum] = "${ACE_TAO_SHA256SUM}"

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

OECONF ??= ""
OECONF:append = "\
    --prefix=${prefix} \
    --verbose \
    --no-tests \
    ${ACE_TAO_OPTION} \
"

OECONF:append:class-target = "\
    --host-tools=${STAGING_BINDIR_NATIVE}/DDS_HOST_ROOT \
    --target=linux-cross \
    --target-compiler=${S}/${HOST_PREFIX}g++ \
"
OECONF:append:class-native = "\
    --target=linux \
    --host-tools-only \
"
OECONF:append:class-nativesdk = "\
    --compiler=${S}/${HOST_PREFIX}g++ \
    --target=linux \
    --host-tools-only \
"

do_configure() {
    cp ${DL_DIR}/${ACE_TAO_FILE} ${S}
    ./configure ${OECONF}
}

do_install:append:class-target() {
    rm ${D}${datadir}/dds/dds/Version.h
    cp ${D}${includedir}/dds/Version.h ${D}${datadir}/dds/dds

    sed -i -e s:${D}/::g ${D}${datadir}/dds/dds-devel.sh

    # workaround: /usr/share/dds/dds/idl/IDLTemplate.txt should be placed into target sysroot
    install -d ${D}${datadir}/dds/dds/idl
    cp ${S}/dds/idl/IDLTemplate.txt ${D}${datadir}/dds/dds/idl

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

do_install:append:class-native() {
    rm ${D}${datadir}/dds/bin/opendds_idl
    rm ${D}${datadir}/ace/bin/ace_gperf
    rm ${D}${datadir}/ace/bin/tao_idl
}

do_install:append:class-nativesdk() {
    ln -sf ${bindir}/opendds_idl ${D}${datadir}/dds/bin/opendds_idl
    ln -sf ${bindir}/ace_gperf ${D}${datadir}/ace/bin/ace_gperf
    ln -sf ${bindir}/tao_idl ${D}${datadir}/ace/bin/tao_idl
}

INSANE_SKIP:${PN} += "dev-so"

FILES:SOLIBSDEV = ""
FILES:${PN} += "${libdir}/*.so"
FILES:${PN}-dev += "${datadir}"

BBCLASSEXTEND = "native nativesdk"
