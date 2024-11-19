# AEPatcher
Плагин поддержки патчей ApkEditor для [Apktool M](https://maximoff.su/apktool "Apktool M"). Поддерживается начиная с версии Apktool M 2.4.0-240217 и выше.

Движок патчей портирован из [исходного кода ApkEditor](https://github.com/timscriptov/ApkEditor "исходного кода ApkEditor").

Скачать готовый APK можно по ссылке: https://maximoff.su/apktool/AEPatcher.apk?b=7

Пример запуска плагина:
```java
/* Создание намерения с обязательными параметрами */
Intent patcherIntent = new Intent(Intent.ACTION_VIEW);
patcherIntent.addCategory(Intent.CATEGORY_DEFAULT);
patcherIntent.setDataAndType(Uri.EMPTY, "application/ru.maximoff.aepatcher-patch"); // mime-тип, заданный в манифесте плагина для его идентификации
patcherIntent.putExtra("projectPath", "/storage/emulated/0/app_src"); // путь к папке с проектом
patcherIntent.putExtra("patchPath", "/storage/emulated/0/patch.zip"); // путь к файлу патча

/* Необязательные параметры */
patcherIntent.setPackage("ru.maximoff.aepatcher"); // имя пакета плагина для запуска конкретного экземпляра
patcherIntent.putExtra("appTheme", 0); // тема диалога плагина: 0 – светлая (по умолчанию), 1 – тёмная, 2 – чёрная
patcherIntent.putExtra("appLanguage", "ru"); // код языка интерфейса плагина
patcherIntent.putExtra("keepScreenOn", true); // флаг, указывающий, нужно ли держать экран включенным во время работы плагина
patcherIntent.putExtra("apkPath", "/storage/emulated/0/app.apk"); // путь к исходному файлу apk

/* Установка флагов и запуск */
patcherIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
context.startActivity(patcherIntent);
```