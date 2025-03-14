import os
import subprocess
import threading
import sys

def stream_reader(pipe):
    """Olvassa a megadott pipe (stdout vagy stderr) tartalmát és írja a konzolra."""
    for line in iter(pipe.readline, b''):
        sys.stdout.write(line.decode())
    pipe.close()

def get_jar_path():

    tester_dir = os.path.dirname(__file__)
    jar_folder = os.path.join(tester_dir, "..", "build", "libs")
    jar_files = [f for f in os.listdir(jar_folder) if f.endswith(".jar")]
    if not jar_files:
        raise FileNotFoundError(f"Nem található jar fájl a {jar_folder} mappában.")
    return os.path.join(jar_folder, jar_files[0])

def main():
    try:
        jar_file = get_jar_path()
    except FileNotFoundError as e:
        print(e)
        return

    print(f"Használt jar fájl: {jar_file}")
    cmd = ["java", "-jar", jar_file]

    try:
        process = subprocess.Popen(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, stdin=subprocess.PIPE)
    except FileNotFoundError:
        print("Hiba: A 'java' parancs nem található, vagy a jar fájl nem létezik.")
        return

    stdout_thread = threading.Thread(target=stream_reader, args=(process.stdout,), daemon=True)
    stderr_thread = threading.Thread(target=stream_reader, args=(process.stderr,), daemon=True)
    stdout_thread.start()
    stderr_thread.start()

    print("Debugger elindult. Írd be a parancsokat, vagy 'exit' a kilépéshez.")

    try:
        while True:
            command = input("Debugger> ")
            if command.strip().lower() == "exit":
                process.terminate()
                break
            process.stdin.write((command + "\n").encode())
            process.stdin.flush()
    except KeyboardInterrupt:
        process.terminate()
        print("\nDebugger leállítva.")

if __name__ == "__main__":
    main()
