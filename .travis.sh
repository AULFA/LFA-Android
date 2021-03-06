#!/bin/sh

info()
{
  echo "info: $1" 1>&2
}

if [ -z "${LFA_BUILDS_SSH_KEY}" ]
then
  echo "LFA_BUILDS_SSH_KEY not set"
  exit 1
fi

if [ -z "${LFA_KEYSTORE_PASSWORD}" ]
then
  echo "LFA_KEYSTORE_PASSWORD not set"
  exit 1
fi

#------------------------------------------------------------------------
# Configure SSH

info "configuring ssh"

mkdir -p "${HOME}/.ssh" || exit 1
echo "${LFA_BUILDS_SSH_KEY}" | base64 -d > "${HOME}/.ssh/id_ed25519" || exit 1
chmod 700 "${HOME}/.ssh" || exit 1
chmod 600 "${HOME}/.ssh/id_ed25519" || exit 1

(cat <<EOF
[builds.lfa.one]:1022 ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIH/vroEIxH46lW/xg+CmCDwO7FHN24oP+ad4T/OtB/D2
EOF
) >> "$HOME/.ssh/known_hosts" || exit 1

#------------------------------------------------------------------------
# Configure Nexus and keystore

info "downloading keystore"

scp -B -P 1022 travis-ci@builds.lfa.one:lfa-keystore.jks . || exit 1

(cat <<EOF

au.org.libraryforall.keyAlias=main
au.org.libraryforall.keyPassword=${LFA_KEYSTORE_PASSWORD}
au.org.libraryforall.storePassword=${LFA_KEYSTORE_PASSWORD}

org.gradle.daemon=true
org.gradle.configureondemand=true
org.gradle.jvmargs=-Xmx4g -XX:MaxPermSize=2048m -XX:+HeapDumpOnOutOfMemoryError
EOF
) >> gradle.properties || exit 1

#------------------------------------------------------------------------
# Configure bundled credentials

info "downloading credentials"

scp -B -P 1022 travis-ci@builds.lfa.one:online-app-credentials.json .    || exit 1
scp -B -P 1022 travis-ci@builds.lfa.one:lfaAnalyticsConfiguration.xml .  || exit 1

VARIANTS="online png_offline grande laos laos.online timor ethiopia"

for VARIANT in ${VARIANTS}
do
  cp online-app-credentials.json   one.lfa.android.app.${VARIANT}/src/main/assets/account_bundled_credentials.json || exit 1
  cp lfaAnalyticsConfiguration.xml one.lfa.android.app.${VARIANT}/src/main/assets/lfaAnalytics.xml                 || exit 1
done

#------------------------------------------------------------------------
# Downloading bundles

info "downloading bundles"

scp -B -P 1022 travis-ci@builds.lfa.one:/feeds/png-feedsonly.zip . || exit 1
mkdir -p one.lfa.android.app.grande/bundles
mkdir -p one.lfa.android.app.online/bundles
cp png-feedsonly.zip one.lfa.android.app.grande/bundles/offline.zip || exit 1
cp png-feedsonly.zip one.lfa.android.app.online/bundles/offline.zip || exit 1

#------------------------------------------------------------------------
# Build!

info "building"

./gradlew clean assembleRelease test || exit 1

#------------------------------------------------------------------------
# Publish APKs

info "publishing APKs"

mkdir -p apk

for VARIANT in ${VARIANTS}
do
  cp -v ./one.lfa.android.app.${VARIANT}/build/outputs/apk/release/*.apk apk/ || exit 1
done

info "rsyncing APKs"
rsync -a -L -i --delay-updates --partial --no-inc-recursive --no-times -e "ssh -p 1022" apk/ travis-ci@builds.lfa.one:/repository/testing/all/
