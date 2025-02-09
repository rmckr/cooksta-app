# Cooksta

A platform for sharing delicious recipes with a social-media-like feed and smart features.

## Getting started

Clone this repository:
```shell
git clone https://gitlab.dit.htwk-leipzig.de/cooksta/cooksta-app.git
cd cooksta-app
```
Alternatively, use SSH to clone: `git@gitlab.dit.htwk-leipzig.de:cooksta/cooksta-app.git`.


### Installing

Check out the [official documentation](https://developer.android.com/studio/run)
on how to run an app in Android Studio if you are unsure.

### Port forwarding

To allow the app to communicate with the API backend, you need to forward TCP port 3000 to your device/emulator:

```shell
adb reverse tcp:3000 tcp:3000
```

## Usage

### Logging in

The API provides a test user for logging in with the following credentials:

Username: `TestUser` \
Password: `cooksta1`


