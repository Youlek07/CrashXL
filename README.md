# CRASHXL

# Autor: JULIAN MACHOWSKI 5TP

## Jak uruchomić program?

Aby uruchomić program, należy wykonać następujące polecenia w terminalu, w folderze projektu (/src):

**1. Skompilowanie plików:**
```bash
javac CrashXL.java Config.java Log.java
```

**2. Utworzenie pliku konfiguracyjnego:**
```bash
java CrashXL --init
```

**3. Uruchomienie programu:**
```bash
java CrashXL
```

## Konfiguracja

Program korzysta z następujących ustawień:

- **C** - ścieżka do pliku konfiguracyjnego
- **R** - główny adres serwera
- **A** - alternatywny adres serwera
- **S** - lista plików do pobrania (`scripts.lst`)
- **H** - folder, do którego pobierane są pliki
- **X** - folder, do którego kopiowany jest program
- **L** - ścieżka do pliku logów

## Działanie programu

- Pobiera listę plików z serwera.
- Pobiera pliki z głównego serwera.
- W przypadku problemów próbuje pobrać pliki z serwera alternatywnego, czyli _A_.
- Zapisuje informacje o działaniu i błędach w pliku logów.
- Sprawdza wersję programu i w razie potrzeby kopiuje nowszą wersję.
