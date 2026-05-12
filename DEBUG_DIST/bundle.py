import zipfile
import os
os.chdir(r'c:\dumpp 127\ITAMS\IT-Assets-Management-System\DEBUG_DIST')
with zipfile.ZipFile('bundle.zip', 'w', zipfile.ZIP_DEFLATED) as z:
    for root, dirs, files in os.walk('.'):
        for f in files:
            if f not in ['bundle.zip', 'RunJAR.cs']:
                path = os.path.join(root, f)
                arcname = path.lstrip('.\\')
                z.write(path, arcname)
