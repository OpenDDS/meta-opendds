Building OpenDDS with Yocto and Kas
===================================

[Kas](https://kas.readthedocs.io/en/latest/index.html) is a setup tool for bitbake based projects.

Prerequisits
------------

# Kas installed

See: https://kas.readthedocs.io/en/latest/userguide.html#dependencies-installation

# Install bmap-tool to create the SDCard

```
apt install bmap-tools
```

# clone the OpenDDS Yocto layer

```
git clone https://github.com/OpenDDS/meta-opendds.git
```

Building
--------

```
cd meta-opendds
kas build kas/rpi.yaml; # Just OpenDDS for RPi
kas build kas/rpi.yaml:kas/ishapes.yaml; # iShapes demo
kas build kas/rpi.yaml:kas/ptests.yaml; # OpenDDS with Yocto ptests
```

Deploying
---------

Copy the WIC artifact to an SDCard.

Following instruction is an *example*.  Check before executing the command where the SDCard is mounted (e.g. `/dev/sda`)!

```
cd build/tmp/deploy/images/raspberrypi4-64
sudo bmaptool copy core-image-minimal-xfce-raspberrypi4-64.rootfs.wic.bz2 /dev/sda
```

Testing
-------

### Run the ishapes demo

Open a terminal on the Raspberry Pi.
Run `ishapes`

### Running the Yocto ptests for OpenDDS

Open a terminal on the Raspberry Pi.
Run `ptest-runner opendds`

A detailed log of the run can be found on the `/tmp` of the device.
