# Ensure that the versioned recipe is always preferred over this one.

# In order to use this recipe, add the following to either local.conf
# or an image recipe.
#
#  PREFERRED_VERSION:opendds="1.0+git%"
#
# It is not guaranteed to build. At any time the HEAD of master may not
# be compatible with the general build instructions in opendds.inc.

DEFAULT_PREFERENCE = "-1"

# Checkout the head of the master branch
SRCREV = "${AUTOREV}"
PV = "1.0+git${SRCPV}"

DDS_SRC_BRANCH ??= "master"
SRC_URI = "git://github.com/objectcomputing/OpenDDS.git;protocol=https;branch=${DDS_SRC_BRANCH}"

OECONF = " \
    --ace-github-latest \
"

ACE_TAO_OPTION = ""

do_configure[network] = "1"

require opendds.inc

# DOC_TAO2_VERSION = "6.5.16"
# DOC_TAO2_SHA256SUM = "c10a89cae08bf9b6d7bda166a1e35d3a737b726544c276b20b3cc7eb9c75a363"
# DOC_TAO2_URI = "https://github.com/DOCGroup/ACE_TAO/releases/download/ACE+TAO-${@'${DOC_TAO2_VERSION}'.replace('.','_')}/ACE+TAO-src-${DOC_TAO2_VERSION}.tar.gz"
# SRC_URI += "${DOC_TAO2_URI};name=ace_tao;unpack=0;subdir=git"
# SRC_URI[ace_tao.sha256sum] = "${DOC_TAO2_SHA256SUM}"

do_install:append:class-target() {
    sed -i -e s:${S}/::g ${D}${libdir}/cmake/OpenDDS/config.cmake
}

do_install:append:class-native() {
    # Prepare HOST_ROOT expected by DDS for target build
    mkdir -p ${D}${bindir}/DDS_HOST_ROOT/ACE_TAO/ACE/bin
    mkdir -p ${D}${bindir}/DDS_HOST_ROOT/bin
    ln -sr ${D}${bindir}/opendds_idl ${D}${bindir}/DDS_HOST_ROOT/bin/opendds_idl
    ln -sr ${D}${bindir}/ace_gperf ${D}${bindir}/DDS_HOST_ROOT/ACE_TAO/ACE/bin/ace_gperf
    ln -sr ${D}${bindir}/tao_idl ${D}${bindir}/DDS_HOST_ROOT/ACE_TAO/ACE/bin/tao_idl
}
