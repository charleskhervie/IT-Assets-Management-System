using System;
using System.IO;
using System.Diagnostics;
using System.IO.Compression;

class Program {
    static void Main(string[] args) {
        try {
            // Get the path of this launcher EXE
            string launcherPath = System.Reflection.Assembly.GetExecutingAssembly().Location;
            string launcherDir = Path.GetDirectoryName(launcherPath);
            
            Console.WriteLine("Extracting ITAMS application...");
            Console.WriteLine("Launcher location: " + launcherPath);
            
            // Read this EXE and find the ZIP signature
            byte[] exeBytes = File.ReadAllBytes(launcherPath);
            int zipStart = -1;
            for (int i = 0; i < exeBytes.Length - 3; i++) {
                if (exeBytes[i] == 0x50 && exeBytes[i+1] == 0x4B && 
                    exeBytes[i+2] == 0x03 && exeBytes[i+3] == 0x04) {
                    zipStart = i;
                    break;
                }
            }
            
            if (zipStart <= 0) {
                Console.WriteLine("ERROR: Failed to find embedded ZIP");
                Console.ReadLine();
                return;
            }
            
            // Extract to temp directory
            string tempDir = Path.Combine(Path.GetTempPath(), "ITAMS_" + Guid.NewGuid().ToString().Substring(0, 8));
            Directory.CreateDirectory(tempDir);
            Console.WriteLine("Temp directory: " + tempDir);
            
            // Extract ZIP
            using (var ms = new MemoryStream(exeBytes, zipStart, exeBytes.Length - zipStart)) {
                using (var zip = new ZipArchive(ms, ZipArchiveMode.Read)) {
                    zip.ExtractToDirectory(tempDir);
                }
            }
            
            Console.WriteLine("Starting application...");
            
            // Run ITAMS.exe with launcher directory as parameter
            string itamsExe = Path.Combine(tempDir, "ITAMS.exe");
            ProcessStartInfo psi = new ProcessStartInfo {
                FileName = itamsExe,
                Arguments = "exe=" + launcherDir,  // Pass launcher directory
                WorkingDirectory = tempDir,
                UseShellExecute = false
            };
            
            Process p = Process.Start(psi);
            if (p != null) {
                p.WaitForExit();
            }
            
            Console.WriteLine("Cleaning up...");
            try {
                Directory.Delete(tempDir, true);
            } catch { }
        } catch (Exception ex) {
            Console.WriteLine("ERROR: " + ex.Message);
            Console.ReadLine();
        }
    }
}
