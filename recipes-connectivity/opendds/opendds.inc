SUMMARY = "OpenDDS is an open source C++ implementation of the Object Management Group (OMG) Data Distribution Service (DDS)"
HOMEPAGE = "https://opendds.org"
BUGTRACKER = "https://github.com/OpenDDS/OpenDDS/issues"

LICENSE = "OpenDDS"
LIC_FILES_CHKSUM = "file://LICENSE;md5=11ee76f6fb51f69658b5bb8327c50b11"

inherit autotools ptest

SRC_URI:append = " file://run-ptest"

SRC_URI:append = " \
    ${@bb.utils.contains('PACKAGECONFIG', 'tests', 'file://0003-tests-added-dependencies-to-run-all-tests.patch', '', d)} \
    "

PACKAGECONFIG ??= "doc-group2 security ${@bb.utils.contains('DISTRO_FEATURES', 'ptest', 'tests', '', d)}"
PACKAGECONFIG:class-native ??= "doc-group2"
PACKAGECONFIG[security] = "--security, , openssl xerces-c, "
PACKAGECONFIG[ishapes] = "--qt=${STAGING_DIR_NATIVE}${prefix_native} --qt-include=${STAGING_INCDIR},,qtbase qtbase-native"
PACKAGECONFIG[doc-group2] = "--ace-tao=ace6tao2,,,,,doc-group3 doc-group4"
PACKAGECONFIG[doc-group3] = "--ace-tao=ace7tao3,,,,,doc-group2 doc-group4"
PACKAGECONFIG[doc-group4] = "--ace-tao=ace8tao4,,,,,doc-group2 doc-group3"
PACKAGECONFIG[tests] = "--tests --gtest=${WORKDIR}/recipe-sysroot/usr, --no-tests, googletest, perl packagegroup-meta-perl perl-module-tie-array perl-module-english, ,ishapes"

DEPENDS += "\
    cmake-native \
    opendds-native \
    perl-native \
"

RDEPENDS:${PN}-dev += "\
    bash \
    coreutils \
    make \
    perl \
"

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
    --no-rapidjson \
    ${PACKAGECONFIG_CONFARGS} \
"

OECONF:append:class-target = "\
    --host-tools=${STAGING_BINDIR_NATIVE}/DDS_HOST_ROOT \
    --target=linux-cross \
    --target-compiler=${S}/${HOST_PREFIX}g++ \
    ${@bb.utils.contains('PACKAGECONFIG', 'security', '--openssl=${WORKDIR}/recipe-sysroot/usr', '', d)} \
    ${@bb.utils.contains('PACKAGECONFIG', 'security', '--xerces3=${WORKDIR}/recipe-sysroot/usr', '', d)} \
"
OECONF:append:class-native = "\
    --target=linux \
    --host-tools-only \
    --no-security \
"
OECONF:append:class-nativesdk = "\
    --compiler=${S}/${HOST_PREFIX}g++ \
    --target=linux \
    --host-tools-only \
"

SOLIBS = ".so*"
FILES_SOLIBSDEV = ""

do_configure() {
    ./configure ${OECONF}
}

do_install:append:class-target() {
    rm ${D}${datadir}/dds/dds/Version.h
    cp ${D}${includedir}/dds/Version.h ${D}${datadir}/dds/dds

    sed -i -e s:${D}/::g ${D}${datadir}/dds/dds-devel.sh

    # workaround: /usr/share/dds/dds/idl/IDLTemplate.txt should be placed into target sysroot
    install -d ${D}${datadir}/dds/dds/idl
    cp ${S}/dds/idl/IDLTemplate.txt ${D}${datadir}/dds/dds/idl

    # Copy OpenDDS source tree folders to sysroot-destdir/usr/share/DDS_ROOT
    # to support building OpenDDS apps in their own layers
    . ${S}/setenv.sh
    mkdir -p ${D}${datadir}/DDS_ROOT/ACE_wrappers
    cp -r ${DDS_ROOT}/dds ${D}${datadir}/DDS_ROOT
    cp -r ${DDS_ROOT}/MPC ${D}${datadir}/DDS_ROOT
    cp -r ${DDS_ROOT}/tools ${D}${datadir}/DDS_ROOT
    cp -r ${DDS_ROOT}/cmake ${D}${datadir}/DDS_ROOT
    cp -r ${DDS_ROOT}/lib ${D}${datadir}/DDS_ROOT
    cp ${DDS_ROOT}/*.GNU ${D}${datadir}/DDS_ROOT
    cp ${DDS_ROOT}/*.txt ${D}${datadir}/DDS_ROOT
    cp -r ${DDS_ROOT}/ACE_wrappers/ace ${D}${datadir}/DDS_ROOT/ACE_wrappers
    cp -r ${DDS_ROOT}/ACE_wrappers/bin ${D}${datadir}/DDS_ROOT/ACE_wrappers
    cp -r ${DDS_ROOT}/ACE_wrappers/include ${D}${datadir}/DDS_ROOT/ACE_wrappers
    cp -r ${DDS_ROOT}/ACE_wrappers/MPC ${D}${datadir}/DDS_ROOT/ACE_wrappers
    cp -r ${DDS_ROOT}/ACE_wrappers/TAO ${D}${datadir}/DDS_ROOT/ACE_wrappers
    cp -r ${DDS_ROOT}/ACE_wrappers/lib ${D}${datadir}/DDS_ROOT/ACE_wrappers
    cp ${DDS_ROOT}/ACE_wrappers/*.txt ${D}${datadir}/DDS_ROOT/ACE_wrappers

    if [ "${@bb.utils.contains("PACKAGECONFIG", "ishapes", "1", "0", d)}" = "1" ]; then
        install -d ${D}${bindir}
        install -m 0755 ${S}/examples/DCPS/ishapes/ishapes ${D}${bindir}/
    fi
}

do_install:append:class-native() {
    rm ${D}${datadir}/dds/bin/opendds_idl
    rm ${D}${datadir}/ace/bin/ace_gperf
    rm ${D}${datadir}/ace/bin/tao_idl
}

do_install:append:class-native() {
    # Prepare HOST_ROOT expected by DDS for target build
    mkdir -p ${D}${bindir}/DDS_HOST_ROOT/ACE_wrappers/bin
    mkdir -p ${D}${bindir}/DDS_HOST_ROOT/bin
    ln -sr ${D}${bindir}/opendds_idl ${D}${bindir}/DDS_HOST_ROOT/bin/opendds_idl
    ln -sr ${D}${bindir}/ace_gperf ${D}${bindir}/DDS_HOST_ROOT/ACE_wrappers/bin/ace_gperf
    ln -sr ${D}${bindir}/tao_idl ${D}${bindir}/DDS_HOST_ROOT/ACE_wrappers/bin/tao_idl
}

do_install:append:class-nativesdk() {
    ln -sf ${bindir}/opendds_idl ${D}${datadir}/dds/bin/opendds_idl
    ln -sf ${bindir}/ace_gperf ${D}${datadir}/ace/bin/ace_gperf
    ln -sf ${bindir}/tao_idl ${D}${datadir}/ace/bin/tao_idl
}

do_install_ptest() {
    sed -i "s#@PTEST_ARGS@#${@bb.utils.contains('PACKAGECONFIG', 'security', '--security', '', d)}#g" ${D}${PTEST_PATH}/run-ptest

    install -d ${D}${datadir}/DDS_ROOT/
    install -d ${D}${datadir}/DDS_ROOT/tests
    cd ${S}/tests
    tar --no-same-owner \
        --exclude='*.mpc'\
        --exclude='*.o' \
        --exclude='*.h' \
        --exclude='*.cpp' \
        --exclude='*.idl' \
        --exclude='*.inl' \
        --exclude='GNUmakefile*'\
        --exclude='.gitignore'\
        --exclude='.depend*'\
        --exclude='.obj*'\
        -cpf - . \
        | tar --no-same-owner -xpf - -C ${D}${datadir}/DDS_ROOT/tests

    install -d ${D}${datadir}/DDS_ROOT/performance-tests
    cd ${S}/performance-tests
    tar --no-same-owner \
        --exclude='*.mpc'\
        --exclude='*.o' \
        --exclude='*.h' \
        --exclude='*.cpp' \
        --exclude='*.idl' \
        --exclude='*.inl' \
        --exclude='GNUmakefile*'\
        --exclude='.gitignore'\
        --exclude='.depend*'\
        --exclude='.obj*'\
        -cpf - . \
        | tar --no-same-owner -xpf - -C ${D}${datadir}/DDS_ROOT/performance-tests

    install -d ${D}${datadir}/DDS_ROOT/DevGuideExamples
    cd ${S}/DevGuideExamples
    tar --no-same-owner \
        --exclude='*.mpc'\
        --exclude='*.o' \
        --exclude='*.h' \
        --exclude='*.cpp' \
        --exclude='*.idl' \
        --exclude='*.inl' \
        --exclude='GNUmakefile*'\
        --exclude='.gitignore'\
        --exclude='.depend*'\
        --exclude='.obj*'\
        -cpf - . \
        | tar --no-same-owner -xpf - -C ${D}${datadir}/DDS_ROOT/DevGuideExamples

    install -d ${D}${datadir}/DDS_ROOT/examples
    cd ${S}/examples
    tar --no-same-owner \
        --exclude='*.mpc'\
        --exclude='*.o' \
        --exclude='*.h' \
        --exclude='*.cpp' \
        --exclude='*.idl' \
        --exclude='*.inl' \
        --exclude='GNUmakefile*'\
        --exclude='.gitignore'\
        --exclude='.depend*'\
        --exclude='.obj*'\
        -cpf - . \
        | tar --no-same-owner -xpf - -C ${D}${datadir}/DDS_ROOT/examples

    # tests do expect this excutable here
    install -d ${D}${bindir}
    install -m 755 ${S}/tools/repoctl/repoctl ${D}${bindir}

    # A symbolic link because the test script will search the execuables under DDS_ROOT.
    # But because of the --prefix in the configure they are installed under /usr/bin
    ln -s ${bindir} ${D}${datadir}/DDS_ROOT/bin
    install -d ${D}/usr/lib/perl5/5.38.2/PerlACE
    install -m 644 ${S}/ACE_wrappers/bin/PerlACE/*.pm ${D}/usr/lib/perl5/5.38.2/PerlACE
    install -d ${D}/usr/lib/perl5/5.38.2/PerlDDS
    install -m 644 ${S}/bin/PerlDDS/*.pm ${D}/usr/lib/perl5/5.38.2/PerlDDS
    install -d ${D}${datadir}/DDS_ROOT/tools/scripts/modules
    install -m 644 ${S}/tools/scripts/modules/*.pm ${D}${datadir}/DDS_ROOT/tools/scripts/modules

}

PACKAGES += "${PN}-ishapes"

INSANE_SKIP:${PN} += "dev-so"
INSANE_SKIP:${PN}-dev += "libdir"

FILES:${PN}-dev += "${datadir}"
FILES:${PN}-ishapes += "${bindir}/ishapes"
FILES:${PN}-ptest += "${libdir}/perl5/5.38.2/PerlACE ${libdir}/perl5/5.38.2/PerlDDS"

ALLOW_EMPTY:${PN}-ishapes = "1"

BBCLASSEXTEND = "native nativesdk"
