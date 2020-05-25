# Ensure that the versioned recipe is always preferred over this one.

# In order to use this recipe, add the following to either local.conf
# or an image recipe.
#
#  PREFERRED_VERSION_opendds="1.0+git%"
#
# It is not guaranteed to build. At any time the HEAD of master may not
# be compatible with the general build instructions in opendds.inc.

DEFAULT_PREFERENCE = "-1"

# Checkout the head of the master branch
SRCREV = "${AUTOREV}"
PV = "1.0+git${SRCPV}"

OECONF = " \
    --ace-github-latest \
"

require opendds.inc

OECONF_append_class-native = "--host-tools-only"
OECONF_append_class-nativesdk = "--host-tools-only"

do_install_append_class-target() {
    sed -i -e s:${S}/::g ${D}${libdir}/cmake/OpenDDS/config.cmake
}

do_install_append_class-native() {
    # Prepare HOST_ROOT expected by DDS for target build
    mkdir -p ${D}${bindir}/DDS_HOST_ROOT/ACE_TAO/ACE/bin
    mkdir -p ${D}${bindir}/DDS_HOST_ROOT/bin
    ln -sr ${D}${bindir}/opendds_idl ${D}${bindir}/DDS_HOST_ROOT/bin/opendds_idl
    ln -sr ${D}${bindir}/ace_gperf ${D}${bindir}/DDS_HOST_ROOT/ACE_TAO/ACE/bin/ace_gperf
    ln -sr ${D}${bindir}/tao_idl ${D}${bindir}/DDS_HOST_ROOT/ACE_TAO/ACE/bin/tao_idl
}
