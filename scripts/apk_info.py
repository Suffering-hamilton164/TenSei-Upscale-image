import os

apk_folder = "app/build/outputs/apk/debug"

for file in os.listdir(apk_folder):
    if file.endswith(".apk"):
        path = os.path.join(apk_folder, file)

        size = os.path.getsize(path) / (1024 * 1024)

        print(f"APK: {file}")
        print(f"Size: {size:.2f} MB")
