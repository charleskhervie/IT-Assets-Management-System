using System;
using System.Diagnostics;
using System.IO;

class RunJAR {
    static void Main(string[] args) {
        try {
            string exePath = System.Reflection.Assembly.GetExecutingAssembly().Location;
            string exeDir = ResolveExeDirectory(args, exePath);
            string baseDir = AppDomain.CurrentDomain.BaseDirectory;
            string jarPath = Path.Combine(baseDir, "ITAMS.jar");
            string libPath = Path.Combine(baseDir, "lib");
            
            Console.WriteLine("========================================");
            Console.WriteLine("📂 EXE Directory: " + exeDir);
            Console.WriteLine("📂 Working Directory: " + baseDir);
            Console.WriteLine("🔍 Looking for JAR: " + jarPath);
            Console.WriteLine("========================================");
            
            if (!File.Exists(jarPath)) {
                Console.WriteLine("❌ CRITICAL ERROR: ITAMS.jar not found!");
                Console.WriteLine("Press Enter to exit...");
                Console.ReadLine();
                return;
            }
            
            Console.WriteLine("✅ JAR found!");
            Console.WriteLine("✅ Launching Java application with JavaFX...");
            Console.WriteLine("========================================\n");
            
            // Build command with JavaFX module path AND EXE directory
            string javaArgs = "-Dexe.dir=\"" + exeDir + "\" " +
                             "--module-path \"" + libPath + "\" " +
                             "--add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base " +
                             "-jar \"" + jarPath + "\"";
            
            ProcessStartInfo psi = new ProcessStartInfo {
                FileName = "java",
                Arguments = javaArgs,
                WorkingDirectory = baseDir,
                UseShellExecute = false,
                CreateNoWindow = false
            };
            
            Process p = Process.Start(psi);
            if (p != null) {
                p.WaitForExit();
                int exitCode = p.ExitCode;
                Console.WriteLine("\n========================================");
                if (exitCode == 0) {
                    Console.WriteLine("✅ Application closed normally");
                } else {
                    Console.WriteLine("❌ Application crashed (exit code: " + exitCode + ")");
                }
                Console.WriteLine("========================================");
                Console.WriteLine("Press Enter to close...");
                Console.ReadLine();
            }
        } catch (Exception ex) {
            Console.WriteLine("\n❌ CRITICAL ERROR:");
            Console.WriteLine(ex.GetType().Name + ": " + ex.Message);
            Console.WriteLine("\nPress Enter to exit...");
            Console.ReadLine();
        }
    }

    static string ResolveExeDirectory(string[] args, string exePath) {
        foreach (string arg in args) {
            if (!string.IsNullOrWhiteSpace(arg) && arg.StartsWith("exe=", StringComparison.OrdinalIgnoreCase)) {
                string launcherDir = arg.Substring(4).Trim().Trim('"');
                if (!string.IsNullOrWhiteSpace(launcherDir)) {
                    return launcherDir;
                }
            }
        }

        return Path.GetDirectoryName(exePath);
    }
}
