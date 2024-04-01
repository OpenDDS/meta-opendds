#!/bin/bash

set -xe

export DEFAULT_YOCTO_BRANCH="master"

if [[ -z "${YOCTO_RELEASE}" ]]; then
  export YOCTO_RELEASE=$DEFAULT_YOCTO_BRANCH
fi

cd $RUNNER_TEMP
git clone --depth 1 --branch $YOCTO_RELEASE https://git.yoctoproject.org/git/poky
cd poky
git clone --depth 1 --branch $YOCTO_RELEASE git://git.openembedded.org/meta-openembedded
. ./oe-init-build-env build
bitbake-layers add-layer ../meta-openembedded/meta-oe
yocto-check-layer --with-software-layer-signature-check --debug "$GITHUB_WORKSPACE"
