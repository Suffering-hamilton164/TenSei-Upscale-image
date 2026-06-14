const fs = require("fs");

const apk = "./app/build/outputs/apk/debug/app-debug.apk";

if (fs.existsSync(apk)) {
    console.log("APK ready for release");
} else {
    console.log("APK not found");
}
