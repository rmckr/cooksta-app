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

### Running the app

The app relies on a backend server to function.
You either need to run the app using the `release` build variant to use the public server instance
or use the `debug` build variant and provide a server instance locally.

> To select which build variant to build and run, go to **Build** > **Select Build Variant** and select a build variant from the menu. \
> &mdash; [Android Studio documentation](https://developer.android.com/build/build-variants)

#### Using the public server instance

Run the app using the `release` build variant.

The app will automatically connect to the public server instance.

#### Using a local server instance

Run the app using the `debug` build variant.

Make sure to build and run the backend according to the [cooksta-api README](https://gitlab.dit.htwk-leipzig.de/cooksta/cooksta-api#cooksta-api).

To allow the app to communicate with the backend, you need to forward TCP port 3000 to your device/emulator:

```shell
adb reverse tcp:3000 tcp:3000
```

## Usage

### Logging in

The API provides a test user for logging in with the following credentials:

Username: `TestUser` \
Password: `cooksta1`


