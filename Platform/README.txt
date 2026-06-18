PackageManager Service is a system application and requires access to internal PackageManager API which not available in standard android.jar
To compile the project it requires to use custom android.jar with updated "android.content.pm" package (classes are taken from UI 4G framework.jar)

Copy and overwrite "platform" folder to <ANDROID_SDK> folder