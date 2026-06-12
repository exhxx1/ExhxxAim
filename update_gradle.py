import re
with open('app/build.gradle', 'r') as f:
    content = f.read()

signing_config = """
    signingConfigs {
        release {
            storeFile file("exhxx-key.jks")
            storePassword "exhxx123"
            keyAlias "exhxx"
            keyPassword "exhxx123"
        }
    }
"""

build_types = """
    buildTypes {
        release {
            minifyEnabled true
            shrinkResources true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
            signingConfig signingConfigs.release
        }
    }
"""

if "signingConfigs" not in content:
    content = content.replace("android {", "android {" + signing_config)
    content = re.sub(r'buildTypes\s*\{.*?\}', build_types.strip(), content, flags=re.DOTALL)
    
    with open('app/build.gradle', 'w') as f:
        f.write(content)
