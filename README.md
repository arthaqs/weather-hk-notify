# Počasí HK Notify

Android app posílající denní notifikaci v 8:00 s předpovědí počasí pro **Hradec Králové** na období **8.6.–14.6. 2026**.

## Jak to funguje

- **Open-Meteo API** — zdarma, bez API klíče, souřadnice HK: `50.2092, 15.8328`
- **WorkManager** — naplánuje opakovaný úkol každý den v 8:00
- **BootReceiver** — obnoví plánování po restartu telefonu
- Tlačítko v appce spustí notifikaci okamžitě (pro testování)

## Sestavení

Otevři v Android Studiu, build & run na telefonu nebo emulátoru (API 26+).

## Permissions

- `INTERNET` — stažení počasí
- `POST_NOTIFICATIONS` — zobrazení notifikace (Android 13+)
- `RECEIVE_BOOT_COMPLETED` — obnova plánovače po restartu
