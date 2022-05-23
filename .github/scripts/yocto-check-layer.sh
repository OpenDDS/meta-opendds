#!/bin/bash

export DEFAULT_YOCTO_BRANCH="master"

if [[ -z "${YOCTO_RELEASE}" ]]; then
  export YOCTO_RELEASE=$DEFAULT_YOCTO_BRANCH
fi

cd $RUNNER_TEMP
git clone --depth 1 --branch $YOCTO_RELEASE https://git.yoctoproject.org/git/poky 
. ./poky/oe-init-build-env build
yocto-check-layer --with-software-layer-signature-check --debug "$GITHUB_WORKSPACE"
