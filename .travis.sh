#!/bin/sh

if [ -z "${LFA_BUILDS_USER}" ]
then
  echo "error: LFA_BUILDS_USER is not defined" 1>&2
  exit 1
fi

if [ -z "${LFA_BUILDS_PASSWORD}" ]
then
  echo "error: LFA_BUILDS_PASSWORD is not defined" 1>&2
  exit 1
fi

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

scp -P 1022 travis-ci@builds.lfa.one:lfa-keystore.jks .

(cat <<EOF

au.org.libraryforall.keyAlias=main
au.org.libraryforall.keyPassword=${LFA_KEYSTORE_PASSWORD}
au.org.libraryforall.storePassword=${LFA_KEYSTORE_PASSWORD}
EOF
) >> gradle.properties || exit 1

#------------------------------------------------------------------------
# Configure bundled credentials

scp -P 1022 travis-ci@builds.lfa.one:online-app-credentials.json .
scp -P 1022 travis-ci@builds.lfa.one:bugsnag.conf .

cp online-app-credentials.json one.lfa.android.app.online/src/main/assets/account_bundled_credentials.json

cp bugsnag.conf one.lfa.android.app.online/src/main/assets/bugsnag.conf

#------------------------------------------------------------------------
# Configure offline bundles

mkdir -p one.lfa.android.app.online/bundles || exit 1

wget \
  --timestamping \
  --user "${LFA_BUILDS_USER}" \
  --password "${LFA_BUILDS_PASSWORD}" \
  --no-verbose \
  --output-document=one.lfa.android.app.online/bundles/offline.zip \
  https://builds.lfa.one/auth/offline-online/offline-online.zip

#------------------------------------------------------------------------
# Build!

./gradlew clean assemble test

#------------------------------------------------------------------------
# Publish APKs

scp -P 1022 ./one.lfa.android.app.online/build/outputs/apk/release/*.apk travis-ci@builds.lfa.one:/sites/builds.lfa.one/apk2/

