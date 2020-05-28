SUMMARY = "OpenDDS is an open source C++ implementation of the Object Management Group (OMG) Data Distribution Service (DDS)"
HOMEPAGE = "https://opendds.org"

LICENSE = "OpenDDS"
LIC_FILES_CHKSUM = "file://LICENSE;md5=11ee76f6fb51f69658b5bb8327c50b11"

DEPENDS += " \
    glib-2.0 \
    boost \
"

RDEPENDS_${PN}-dev += "perl"

DDS_SRC_BRANCH ??= "master"
SRC_URI = "git://github.com/objectcomputing/OpenDDS.git;protocol=https;branch=${DDS_SRC_BRANCH}"

S = "${WORKDIR}/git"

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

BASECONF ??= ""
BASECONF += "\
    --prefix=${prefix} \
    --no-tests \
"

OECONF_class-target = "\
    ${BASECONF} \
    --target=linux-cross \
    --target-compiler=${S}/${HOST_PREFIX}g++ \
"
OECONF_class-native = "\
    ${BASECONF} \
    --target=linux \
"
OECONF_class-nativesdk = "\
    ${BASECONF} \
    --target=linux \
"

do_configure() {
    # the configure script assumes those environment variables are for host
    # but bitbake sets them for target, so switch
    export CC=$BUILD_CC
    export LD=$BUILD_LD
    export CXX=$BUILD_CXX
    export AR=$BUILD_AR

    cd ${S}
    ./configure ${OECONF}
}

do_compile() {
    # the configure script assumes those environment variables are for host
    # but bitbake sets them for target, so switch
    export CC=$BUILD_CC
    export LD=$BUILD_LD
    export CXX=$BUILD_CXX
    export AR=$BUILD_AR

    cd ${S}
    oe_runmake
}

DIR_FROM_class-target ??= "${S}/build/target"
DIR_FROM_class-native ??= "${S}"
DIR_FROM_class-nativesdk ??= "${S}"

do_install() {
    # the configure script assumes those environment variables are for host
    # but bitbake sets them for target, so switch
    export CC=$BUILD_CC
    export LD=$BUILD_LD
    export CXX=$BUILD_CXX
    export AR=$BUILD_AR

    cd ${DIR_FROM}
    oe_runmake install DESTDIR=${D}
}

do_install_append_class-target() {
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

FILES_${PN} = " \
    ${libdir} \
    ${bindir} \
"
FILES_${PN}-dev += " \
    ${libdir}/cmake/OpenDDS/* \
    ${includedir} \
    ${datadir} \
"

BBCLASSEXTEND = "native nativesdk"
