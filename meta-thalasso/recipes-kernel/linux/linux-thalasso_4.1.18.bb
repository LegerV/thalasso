# linux-yocto-custom.bb:
#
#   An example kernel recipe that uses the linux-yocto and oe-core
#   kernel classes to apply a subset of yocto kernel management to git
#   managed kernel repositories.
#
#   To use linux-yocto-custom in your layer, copy this recipe (optionally
#   rename it as well) and modify it appropriately for your machine. i.e.:
#
#     COMPATIBLE_MACHINE_yourmachine = "yourmachine"
#
#   You must also provide a Linux kernel configuration. The most direct
#   method is to copy your .config to files/defconfig in your layer,
#   in the same directory as the copy (and rename) of this recipe and
#   add file://defconfig to your SRC_URI.
#
#   To use the yocto kernel tooling to generate a BSP configuration
#   using modular configuration fragments, see the yocto-bsp and
#   yocto-kernel tools documentation.
#
# Warning:
#
#   Building this example without providing a defconfig or BSP
#   configuration will result in build or boot errors. This is not a
#   bug.
#
#
# Notes:
#
#   patches: patches can be merged into to the source git tree itself,
#            added via the SRC_URI, or controlled via a BSP
#            configuration.
#
#   defconfig: When a defconfig is provided, the linux-yocto configuration
#              uses the filename as a trigger to use a 'allnoconfig' baseline
#              before merging the defconfig into the build. 
#
#              If the defconfig file was created with make_savedefconfig, 
#              not all options are specified, and should be restored with their
#              defaults, not set to 'n'. To properly expand a defconfig like
#              this, specify: KCONFIG_MODE="--alldefconfig" in the kernel
#              recipe.
#   
#   example configuration addition:
#            SRC_URI += "file://smp.cfg"
#   example patch addition (for kernel v4.x only):
#            SRC_URI += "file://0001-linux-version-tweak.patch"
#   example feature addition (for kernel v4.x only):
#            SRC_URI += "file://feature.scc"
#

inherit kernel
require recipes-kernel/linux/linux-yocto.inc


LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://${WORKDIR}/COPYING.GPL;md5=751419260aa954499f7abaabaa882bbe"

# Override SRC_URI in a copy of this recipe to point at a different source
# tree if you do not want to build from Linus' tree.
SRC_URI = "https://git.xenomai.org/xenomai-3.git/snapshot/xenomai-3-3.0.3.tar.bz2;name=xenomai \
           https://www.kernel.org/pub/linux/kernel/v4.x/linux-4.1.18.tar.gz;name=kernel \
           file://COPYING.GPL "

SRC_URI_append_qemu_x86-64 = " file://arch/x86/defconfig"
SRC_URI_append_x86-64 = " file://arch/x86/defconfig"
SRC_URI_append_qemu_arm = " file://arch/arm/defconfig"
SRC_URI_append_arm = " file://arch/arm/defconfig"
SRC_URI_append_qemuall = " file://cfg/virtio"

SRC_URI[kernel.sha256sum] = "cf887937742d2b6a167b0dd56152f8271914caa5108868b29d16e212a8e09aae"
SRC_URI[xenomai.sha256sum] = "2770163240bd8aa957a7ab38ee2d83c0cd4c25297578e1de00ca6df382f0f62d"

S = "${WORKDIR}/linux-4.1.18"

LINUX_VERSION ?= "4.1.18"
LINUX_VERSION_EXTENSION_append = "-thalasso"

# Modify SRCREV to a different commit hash in a copy of this recipe to
# build a different release of the Linux kernel.
# tag: v4.2 64291f7db5bd8150a74ad2036f1037e6a0428df2
#SRCREV_machine="64291f7db5bd8150a74ad2036f1037e6a0428df2"
SRCREV_machine="83fdace666f72dbfc4a7681a04e3689b61dae3b9"
#SRCREV_machine="007f4f8ccbda6c5c3de5133ef39324b605f0e074"


PV = "${LINUX_VERSION}+xenomai"

# Override COMPATIBLE_MACHINE to include your machine in a copy of this recipe
# file. Leaving it empty here ensures an early explicit build failure.
COMPATIBLE_MACHINE = "qemux86-64|qemuarm"

do_validate_branches() {
}

do_kernel_metadata_prepend(){
find ${WORKDIR}/arch -name defconfig -exec cp {} ${WORKDIR} \;
}

do_configure_prepend(){
XENODIR=$(find ${WORKDIR} -maxdepth 1 -name "xenomai-3*")
LINUXDIR=$(find ${WORKDIR} -maxdepth 1 -name "linux-4.1.18")
echo ${WORKDIR}
echo $XENODIR $LINUXDIR
cp $LINUXDIR/include/linux/compiler-gcc5.h $LINUXDIR/include/linux/compiler-gcc6.h
$XENODIR/scripts/prepare-kernel.sh --linux=$LINUXDIR --ipipe=$XENODIR/kernel/cobalt/arch/x86/patches/ipipe-core-4.1.18-${ARCH}* --arch=${ARCH}
}

KERNEL_FEATURES_append_qemuall=" ${WORKDIR}/cfg/virtio"

addtask do_configure_preprend before do_kernel_configcheck
