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

OECONF:append = "\
    --ace-github-latest \
"

do_configure[network] = "1"

require opendds.inc

do_install:append:class-target() {
    sed -i -e s:${S}/::g ${D}${libdir}/cmake/OpenDDS/config.cmake
}
