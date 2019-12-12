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
EOF
) >> gradle.properties || exit 1

#------------------------------------------------------------------------
# Configure bundled credentials

info "downloading credentials"

scp -B -P 1022 travis-ci@builds.lfa.one:online-app-credentials.json . || exit 1
scp -B -P 1022 travis-ci@builds.lfa.one:bugsnag.conf . || exit 1

cp online-app-credentials.json one.lfa.android.app.online/src/main/assets/account_bundled_credentials.json
cp online-app-credentials.json one.lfa.android.app.grande/src/main/assets/account_bundled_credentials.json

cp bugsnag.conf one.lfa.android.app.online/src/main/assets/bugsnag.conf
cp bugsnag.conf one.lfa.android.app.grande/src/main/assets/bugsnag.conf

#------------------------------------------------------------------------
# Downloading bundles

info "downloading bundles"

scp -B -P 1022 travis-ci@builds.lfa.one:/feeds/png-feedsonly.zip .
mkdir -p one.lfa.android.app.grande/bundles
mkdir -p one.lfa.android.app.online/bundles
cp png-feedsonly.zip one.lfa.android.app.grande/bundles/offline.zip
cp png-feedsonly.zip one.lfa.android.app.online/bundles/offline.zip

#------------------------------------------------------------------------
# Build!

info "building"

./gradlew clean assemble test || exit 1

#------------------------------------------------------------------------
# Publish APKs

info "publishing APKs"

mkdir -p apk
cp -v ./one.lfa.android.app.grande/build/outputs/apk/release/*.apk apk/
cp -v ./one.lfa.android.app.online/build/outputs/apk/release/*.apk apk/

while [ 1 ]
do
  info "rsyncing APKs"

  rsync -avz -e "ssh -p 1022" apk/ travis-ci@builds.lfa.one:/repository/testing/all/
  if [ $? -eq 0 ]
  then
    exit 0
  else
    sleep 2
  fi
done
